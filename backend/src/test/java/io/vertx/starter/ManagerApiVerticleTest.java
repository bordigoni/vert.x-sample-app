package io.vertx.starter;

import fr.bordigoni.vertx.manager.ManagerApiVerticle;
import fr.bordigoni.vertx.manager.db.DbVerticle;
import fr.bordigoni.vertx.manager.db.client.Client;
import fr.bordigoni.vertx.manager.db.pollsource.PollSource;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

import static java.util.stream.Collectors.toSet;

@RunWith(VertxUnitRunner.class)
public class ManagerApiVerticleTest {

  private Vertx vertx;
  // private int port;
  private WebClient webClient;

  @Before
  public void setUp(final TestContext tc) throws IOException {
    this.vertx = Vertx.vertx();

    final ServerSocket socket = new ServerSocket(0);
    final int port = socket.getLocalPort();
    socket.close();

    this.webClient = WebClient.create(this.vertx, new WebClientOptions()
      .setDefaultHost("localhost")
      .setDefaultPort(port));

    final JsonObject inMemoryDBConfig = new JsonObject()
      .put(DbVerticle.CONFIG_DB_URL, "jdbc:derby:memory:db;create=true");


    this.vertx.deployVerticle(DbVerticle.class.getName(),
      new DeploymentOptions().setConfig(inMemoryDBConfig),
      tc.asyncAssertSuccess());

    this.vertx.deployVerticle(ManagerApiVerticle.class.getName(),
      new DeploymentOptions().setConfig(new JsonObject().put(ManagerApiVerticle.HTTP_PORT, port)),
      tc.asyncAssertSuccess());


  }

  @After
  public void tearDown(final TestContext tc) {
    this.vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void testThatTheServerIsStarted(final TestContext tc) {
    final Async async = tc.async();

    this.webClient.get("/util/ping").send(ar -> {
      tc.assertTrue(ar.succeeded());

      final HttpResponse<Buffer> response = ar.result();

      tc.assertEquals(200, response.statusCode());
      tc.assertEquals("OK", response.bodyAsString());
      async.complete();

    });
  }


  @Test
  public void testThatThePollSourcesAreCreatedThenGetThem(final TestContext tc) {

    final Async async = tc.async();


    final Future<PollSource> create = Future.future();

    {
      final PollSource pollSource = new PollSource("http://www.google.com", 1000);
      this.webClient.post("/pollsource")
        .as(BodyCodec.json(PollSource.class))
        .sendJson(pollSource, ar -> {
          if (ar.succeeded()) {
            final PollSource body = ar.result().body();
            tc.assertNotNull(body);
            tc.assertNotNull(body);
            tc.assertEquals(pollSource.getDelay(), body.getDelay());
            tc.assertEquals(pollSource.getUrl(), body.getUrl());
            tc.assertNotNull(body.getId());
            tc.assertTrue(body.getId().length() > 0);
            create.complete(body);
          } else {
            tc.fail(ar.cause());
          }
        });
    }


    Future<PollSource> get = Future.future();

    create.compose(pollSource -> this.webClient.get("/pollsource/" + pollSource.getId())
      .as(BodyCodec.json(PollSource.class))
      .send(ar -> {
        if (ar.succeeded()) {
          tc.assertEquals(ar.result().body().getId(), pollSource.getId());
          tc.assertEquals(ar.result().body().getDelay(), pollSource.getDelay());
          tc.assertEquals(ar.result().body().getUrl(), pollSource.getUrl());
          get.complete(ar.result().body());
        } else {
          tc.fail(ar.cause());
        }
      }), get);


    Future<Set<String>> create2 = Future.future();
    {
      get.compose(pollSource2 -> {
        final PollSource pollSource = new PollSource("http://www.yahoo.com", 2000);
        this.webClient.post("/pollsource")
          .as(BodyCodec.json(PollSource.class))
          .sendJson(pollSource, ar -> {
            if (ar.succeeded()) {
              create2.complete(new HashSet<>(Arrays.asList(create.result().getId(), ar.result().body().getId())));
            } else {
              tc.fail(ar.cause());
            }
          });
      }, create2);
    }


    Future<Set<String>> delete = Future.future();

    create2.compose(ids -> this.webClient.get("/pollsource")
      .as(BodyCodec.jsonArray())
      .send(ar -> {
        if (ar.succeeded()) {
          JsonArray body = ar.result().body();
          tc.assertNotNull(body);
          tc.assertEquals(2, body.size());
          tc.assertEquals(ids, body.stream().map(o -> ((JsonObject) o).getString("id")).collect(toSet()));
          delete.complete(ids);
        } else {
          tc.fail(ar.cause());
        }
      }), delete);

    delete.compose(ids -> {
      List<String> listOfId = new ArrayList<>(ids);
      this.webClient.delete("/pollsource/" + listOfId.get(0))
        .send(result -> {
          if (result.succeeded()) {
            this.webClient.delete("/pollsource/" + listOfId.get(1))
              .send(result2 -> {
                if (result2.succeeded()) {
                  this.webClient.get("/pollsource")
                    .as(BodyCodec.jsonArray())
                    .send(getAll -> {
                      if (getAll.succeeded()) {
                        tc.assertEquals(0, getAll.result().body().size());
                        async.complete();
                      } else {
                        tc.fail("Error getting all pollsources");
                        async.complete();
                      }
                    });
                } else {
                  tc.fail("Error delete second pollsource");
                  async.complete();
                }
              });
          } else {
            tc.fail("Error delete first pollsource");
            async.complete();
          }
        });
    }, Future.failedFuture("Async.complete() should have been called"));


  }


  @Test
  public void testThatTheClientIsCreatedThenGetIt(final TestContext tc) {

    final Async async = tc.async();

    final Future<Client> create = Future.future();


    {
      final Client client = new Client();
      client.setName("Bordigoni Banking Inc.");
      client.setEmail("benoit@bordigoni.fr");
      client.setPassword("aV3ry$3cr3tPa$$w0rd!");

      this.webClient.post("/client")
        .as(BodyCodec.json(Client.class))
        .sendJson(client, ar -> {
          if (ar.succeeded()) {
            final Client body = ar.result().body();
            tc.assertNotNull(body);
            tc.assertEquals(client.getEmail(), body.getEmail());
            tc.assertEquals(client.getName(), body.getName());
            tc.assertEquals(client.getPassword(), body.getPassword());
            tc.assertNotNull(body.getId());
            tc.assertTrue(body.getId().length() > 0);
            create.complete(body);
          } else {
            tc.fail(ar.cause());
          }
        });
    }

    Future<Client> get = Future.future();

    create.compose(client -> this.webClient.get("/client/" + client.getId())
      .as(BodyCodec.json(Client.class))
      .send(ar -> {
        if (ar.succeeded()) {
          final Client body = ar.result().body();
          tc.assertTrue(body != client);
          tc.assertEquals(client.getEmail(), body.getEmail());
          tc.assertEquals(client.getName(), body.getName());
          tc.assertEquals(client.getPassword(), body.getPassword());
          get.complete(body);
        } else {
          tc.fail(ar.cause());
        }
      }), get);


    final Future<Set<String>> create2 = Future.future();


    {

      get.compose(firstClient -> {

        final Client client = new Client();
        client.setName("Greedy Financial Assets Inc.");
        client.setEmail("greedy@ashell.money");
        client.setPassword("an0therV3ry$3cr3tPa$$w0rd!");

        this.webClient.post("/client")
          .as(BodyCodec.json(Client.class))
          .sendJson(client, ar -> {
            if (ar.succeeded()) {
              final Client body = ar.result().body();
              tc.assertNotNull(body);
              create2.complete(new HashSet<>(Arrays.asList(firstClient.getId(), body.getId())));
            } else {
              tc.fail(ar.cause());
            }
          });
      }, create2);
    }


    create2.compose(ids -> this.webClient.get("/client")
      .as(BodyCodec.jsonArray())
      .send(ar -> {
        if (ar.succeeded()) {
          JsonArray body = ar.result().body();
          tc.assertNotNull(body);
          tc.assertEquals(2, body.size());
          tc.assertEquals(ids, body.stream().map(o -> ((JsonObject) o).getString("id")).collect(toSet()));
          async.complete();
        } else {
          tc.fail(ar.cause());
          async.complete();
        }
      }), Future.failedFuture("async.complete() should have been called"));


  }


}

package io.vertx.starter;

import fr.bordigoni.vertx.manager.ManagerApiVerticle;
import fr.bordigoni.vertx.manager.db.DbVerticle;
import fr.bordigoni.vertx.manager.db.client.Client;
import fr.bordigoni.vertx.manager.db.pollsource.PollSource;
import io.vertx.core.AsyncResult;
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
            tc.assertEquals(1000, body.getDelay());
            tc.assertEquals("http://www.google.com", body.getUrl());
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
          tc.assertEquals(1000, ar.result().body().getDelay());
          tc.assertEquals("http://www.google.com", ar.result().body().getUrl());
          get.complete(ar.result().body());
        } else {
          tc.fail(ar.cause());
        }
      }), get);


    Future<PollSource> update = Future.future();
    get.compose(pollSource -> {
      pollSource.setDelay(2000);
      this.webClient.put("/pollsource/" + pollSource.getId())
        .sendJson(pollSource, ar -> {
          if (ar.succeeded()) {
            tc.assertEquals(200, ar.result().statusCode());
            update.complete(pollSource);
          } else {
            tc.fail(ar.cause());
          }
        });
    }, update);

    Future<PollSource> get2 = Future.future();
    update.compose(pollSource -> this.webClient.get("/pollsource/" + pollSource.getId())
      .as(BodyCodec.json(PollSource.class))
      .send(ar -> {
        if (ar.succeeded()) {
          tc.assertEquals(ar.result().body().getId(), pollSource.getId());
          tc.assertEquals(2000, ar.result().body().getDelay());
          tc.assertEquals("http://www.google.com", ar.result().body().getUrl());
          get2.complete(ar.result().body());
        } else {
          tc.fail(ar.cause());
        }
      }), get2);

    Future<Set<String>> create2 = Future.future();
    {
      get2.compose(pollSource2 -> {
        final PollSource pollSource = new PollSource("http://www.yahoo.com", 3000);
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
      .send(ar -> assertJsonArrayOfIdMatches(tc, delete, ids, ar)), delete);

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
                      }
                    });
                } else {
                  tc.fail("Error delete second pollsource");
                }
              });
          } else {
            tc.fail("Error delete first pollsource");
          }
        });
    }, Future.failedFuture("Async.complete() should have been called"));


  }


  @Test
  public void testThatTheClientsAreCreatedThenGetThem(final TestContext tc) {

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


    Future<Client> update = Future.future();
    {
      get.compose(client -> {
        client.setPassword("root");
        this.webClient.put("/client/" + client.getId())
          .sendJson(client, ar -> {
            if (ar.succeeded()) {
              tc.assertEquals(200, ar.result().statusCode());
              update.complete(client);
            } else {
              tc.fail(ar.cause());
            }
          });

      }, update);
    }

    Future<Client> get2 = Future.future();

    update.compose(client -> this.webClient.get("/client/" + client.getId())
      .as(BodyCodec.json(Client.class))
      .send(ar -> {
        if (ar.succeeded()) {
          tc.assertEquals(ar.result().body().getId(), client.getId());
          tc.assertEquals(ar.result().body().getPassword(), client.getPassword());
          get2.complete(ar.result().body());
        } else {
          tc.fail(ar.cause());
        }
      }), get2);

    final Future<Set<String>> create2 = Future.future();

    {
      get2.compose(firstClient -> {

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

    Future<Set<String>> delete = Future.future();

    create2.compose(ids -> this.webClient.get("/client")
      .as(BodyCodec.jsonArray())
      .send(ar -> assertJsonArrayOfIdMatches(tc, delete, ids, ar)), delete);


    delete.compose(ids -> {
      List<String> listOfId = new ArrayList<>(ids);
      this.webClient.delete("/client/" + listOfId.get(0))
        .send(result -> {
          if (result.succeeded()) {
            this.webClient.delete("/client/" + listOfId.get(1))
              .send(result2 -> {
                if (result2.succeeded()) {
                  this.webClient.get("/client")
                    .as(BodyCodec.jsonArray())
                    .send(getAll -> {
                      if (getAll.succeeded()) {
                        tc.assertEquals(0, getAll.result().body().size());
                        async.complete();
                      } else {
                        tc.fail("Error getting all clients");
                      }
                    });
                } else {
                  tc.fail("Error delete second client");
                }
              });
          } else {
            tc.fail("Error delete first client");
          }
        });
    }, Future.failedFuture("Async.complete() should have been called"));


  }

  private void assertJsonArrayOfIdMatches(TestContext tc, Future<Set<String>> future, Set<String> ids, AsyncResult<HttpResponse<JsonArray>> ar) {
    if (ar.succeeded()) {
      JsonArray body = ar.result().body();
      tc.assertNotNull(body);
      tc.assertEquals(2, body.size());
      tc.assertEquals(ids, body.stream().map(o -> ((JsonObject) o).getString("id")).collect(toSet()));
      future.complete(ids);
    } else {
      tc.fail(ar.cause());
    }
  }


}

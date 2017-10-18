package io.vertx.starter;

import fr.bordigoni.vertx.manager.ManagerApiVerticle;
import fr.bordigoni.vertx.manager.db.DbVerticle;
import fr.bordigoni.vertx.manager.db.pollsource.PollSource;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
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
  public void testThatThePollSourceIsCreated(final TestContext tc) {

    final Async async = tc.async();


    final PollSource config = new PollSource("http://www.google.com", 1000);

    this.webClient.post("/pollsource")
      .as(BodyCodec.json(PollSource.class))
      .sendJson(config, ar -> {
        if (ar.succeeded()) {
          final PollSource body = ar.result().body();
          tc.assertNotNull(body);
          tc.assertEquals(config.getDelay(), body.getDelay());
          tc.assertEquals(config.getUrl(), body.getUrl());
          tc.assertNotNull(body.getId());
          tc.assertTrue(body.getId().length() > 0);
          async.complete();
        } else {
          tc.fail(ar.cause());
          async.complete();
        }
      });

  }

  @Test
  public void testThatThePollSourceIsCreatedThenGetIt(final TestContext tc) {

    final Async async = tc.async();


    final PollSource pollSource = new PollSource("http://www.google.com", 1000);

    final Future<PollSource> createFuture = Future.future();

    this.webClient.post("/pollsource")
      .as(BodyCodec.json(PollSource.class))
      .sendJson(pollSource, ar -> {
        if (ar.succeeded()) {
          final PollSource body = ar.result().body();
          tc.assertNotNull(body.getId());
          tc.assertTrue(body.getId().length() > 0);
          createFuture.complete(body);
        } else {
          tc.fail(ar.cause());
        }
      });

    createFuture.compose(handler -> {
      this.webClient.get("/pollsource/" + createFuture.result().getId())
        .as(BodyCodec.json(PollSource.class))
        .send(ar -> {
          if (ar.succeeded()) {
            final PollSource body = ar.result().body();
            tc.assertTrue(body != pollSource);
            tc.assertTrue(body.getId() != pollSource.getId());
            tc.assertTrue(body.getDelay() != pollSource.getDelay());
            tc.assertTrue(body.getUrl() != pollSource.getUrl());
          } else {
            tc.fail(ar.cause());
          }
          async.complete();
        });
    }, Future.failedFuture("Should not get here..."));

  }

}

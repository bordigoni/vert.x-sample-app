package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

public class GreetingService extends AbstractVerticle {


  public static void main(String... args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(GreetingService.class.getName());
  }

  @Override
  public void start() {

    WebClient client = WebClient.create(vertx);

    vertx.eventBus().consumer("greeting-service", msg -> {
      client.get(8081, "localhost", "/")
        .send(ar -> {
          if (ar.succeeded()) {
            String message = ar.result().bodyAsJsonObject().getString("message");
            msg.reply("Hello " + message + "\n");
          } else {
            msg.fail(0, ar.cause().getLocalizedMessage());
          }
        });
    });

  }

}

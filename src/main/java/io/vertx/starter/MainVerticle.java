package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class MainVerticle extends AbstractVerticle {


  public static void main(String... args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(MainVerticle.class.getName());
  }

  @Override
  public void start() {

    vertx.createHttpServer()
      .requestHandler(req -> {
        vertx.eventBus().<String>send("greeting-service", "whatever", ar -> {
          if (ar.succeeded()) {
            req.response().end(ar.result().body());
          } else {
            System.err.println("Error : " + ar.result().body());
            req.response().setStatusCode(500).end();
          }
        });
      }).listen(8080);
  }

}

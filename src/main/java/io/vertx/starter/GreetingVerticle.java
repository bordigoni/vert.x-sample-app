package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class GreetingVerticle extends AbstractVerticle {


  public static void main (String... args){
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(GreetingVerticle.class.getName());
  }


  @Override
  public void start() {
    vertx.createHttpServer()
        .requestHandler(req -> req.response().end(new JsonObject().put("message", "Vert.x").encode()))
        .listen(8081);
  }

}

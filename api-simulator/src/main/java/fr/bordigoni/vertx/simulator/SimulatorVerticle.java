package fr.bordigoni.vertx.simulator;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.CorsHandler;

import java.util.Random;

public class SimulatorVerticle extends AbstractVerticle {

  private static final String CONFIG_HTTP_PORT = "http.port";
  private Random random;
  private JsonObject stockValue;


  @Override
  public void start() throws Exception {

    random = new Random();

    Router router = Router.router(vertx);

    router.route().handler(CorsHandler.create("*")
      .allowedHeader("content-type")
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.OPTIONS));

    router.get("/stocks/:code").handler(this::stocks);

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(config().getInteger(CONFIG_HTTP_PORT, 10000));

  }

  private void stocks(RoutingContext routingContext) {

    String code = routingContext.pathParam("code");

    if (stockValue == null || shouldGenerateANewValue()) {
      stockValue = new JsonObject()
        .put("timestamp", System.currentTimeMillis())
        .put("code", code)
        .put("value", Math.random() * 100);
    }

    routingContext.response()
      .putHeader("Content-Type", "application/json")
      .end(stockValue.encode());


  }


  private boolean shouldGenerateANewValue() {
    return random.nextBoolean();
  }

}

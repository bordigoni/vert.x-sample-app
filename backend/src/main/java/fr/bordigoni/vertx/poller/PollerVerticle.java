package fr.bordigoni.vertx.poller;

import fr.bordigoni.vertx.manager.db.pollsource.PollSourceService;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.CorsHandler;

import java.util.Objects;

/**
 * Created by benoit on 15/11/2017.
 */
public class PollerVerticle extends AbstractVerticle {

  private PollSourceService service;

  @Override
  public void start(Future<Void> start) throws Exception {

    this.service = PollSourceService.createProxy(vertx.getDelegate());

    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create("*")
      .allowedMethod(HttpMethod.OPTIONS)
      .allowedMethod(HttpMethod.GET));

    router.get("/poll-for-me/:clientId/:id").handler(this::pollForMe);

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .rxListen(8000)
      .subscribe(id -> start.complete());


  }

  private void pollForMe(RoutingContext routingContext) {

    routingContext.response().setChunked(true);

    this.service.get(routingContext.pathParam("id"), pollSource -> {
      if (pollSource.succeeded()) {
        if (pollSource.result().getClientId().equals(routingContext.pathParam("clientId"))) {
          new Poller(vertx, pollSource.result())
            .poll()
            .subscribe(
              json -> routingContext.response()
                .end(json.encode()),
              err -> routingContext.response()
                .setChunked(false)
                .setStatusCode(500)
                .end(Objects.toString(pollSource.cause())));
        } else {
          routingContext.response()
            .setChunked(false)
            .setStatusCode(400)
            .end("Client ID don't match");
        }
      } else {
        routingContext.response()
          .setChunked(false)
          .setStatusCode(500)
          .end(pollSource.cause().getMessage());
      }
    });

  }
}

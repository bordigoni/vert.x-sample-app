package fr.bordigoni.vertx.manager;

import fr.bordigoni.vertx.manager.db.pollsource.PollSource;
import fr.bordigoni.vertx.manager.db.pollsource.PollSourceService;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by benoit on 18/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
public class ManagerRoutesHandlers {

  private static final Logger logger = LoggerFactory.getLogger(ManagerRoutesHandlers.class);


  private final PollSourceService pollSourceService;

  public ManagerRoutesHandlers(final PollSourceService pollSourceService) {
    this.pollSourceService = pollSourceService;
  }

  public void savePollSource(final RoutingContext routingContext) {

    @Nullable final JsonObject bodyAsJson = routingContext.getBodyAsJson();
    this.pollSourceService.save(bodyAsJson.mapTo(PollSource.class), handler -> {
      if (handler.succeeded()) {
        this.logger.info("PollSource saved : {}", JsonObject.mapFrom(handler.result()));
        routingContext.response().putHeader("Content-Type", "application/json")
          .setStatusCode(201)
          .end(JsonObject.mapFrom(handler.result()).toString());
      } else {
        routingContext.response().setStatusCode(500).end();
      }
    });
  }


  public void getSource(final RoutingContext routingContext) {
    this.pollSourceService.get(routingContext.pathParam("id"), result -> {
      if (result.succeeded()) {
        routingContext.response()
          .putHeader("Content-Type", "application/json")
          .end(JsonObject.mapFrom(result.result()).encode());
      } else {

      }
    });
  }

  public void ping(final RoutingContext rc) {
    rc.response().putHeader("Content-Type", "plain/text").end("OK");
  }

}

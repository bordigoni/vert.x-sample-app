package fr.bordigoni.vertx.manager;

import fr.bordigoni.vertx.manager.db.client.Client;
import fr.bordigoni.vertx.manager.db.client.ClientService;
import fr.bordigoni.vertx.manager.db.pollsource.PollSource;
import fr.bordigoni.vertx.manager.db.pollsource.PollSourceService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

/**
 * Created by benoit on 18/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */

class ManagerRoutesHandlers {

  private static final Logger LOG = LoggerFactory.getLogger(ManagerRoutesHandlers.class);

  private final PollSourceService pollSourceService;
  private final ClientService clientService;


  ManagerRoutesHandlers(final PollSourceService pollSourceService, ClientService clientService) {
    this.pollSourceService = pollSourceService;
    this.clientService = clientService;
  }



  void savePollSource(final RoutingContext routingContext) {

    final JsonObject bodyAsJson = routingContext.getBodyAsJson();
    this.pollSourceService.save(bodyAsJson.mapTo(PollSource.class), handler -> {
      if (handler.succeeded()) {
        LOG.debug("PollSource saved : {}", JsonObject.mapFrom(handler.result()));
        routingContext.response().putHeader("Content-Type", "application/json")
          .setStatusCode(201)
          .end(JsonObject.mapFrom(handler.result()).toString());
      } else {
        LOG.error("Error saving pollSource", handler.cause());
        routingContext.response().setStatusCode(500).end();
      }
    });
  }

  void deletePollSource(RoutingContext routingContext) {
    this.pollSourceService.delete(routingContext.pathParam("id"), result -> {
      if (result.succeeded()) {
        routingContext.response()
          .setStatusCode(200).end();
      } else {
        LOG.error("Error deleting pollSource", result.cause());
        routingContext.response().setStatusCode(500).end();
      }
    });
  }

  void getPollSource(final RoutingContext routingContext) {
    this.pollSourceService.get(routingContext.pathParam("id"), result -> {
      if (result.succeeded()) {
        routingContext.response()
          .putHeader("Content-Type", "application/json")
          .end(JsonObject.mapFrom(result.result()).encode());
      } else {
        LOG.error("Error saving pollSource", result.cause());
        routingContext.response().setStatusCode(500).end();
      }
    });
  }

  void getAllPollSources(final RoutingContext routingContext) {
    this.pollSourceService.getAll(serviceCall -> {
      if (serviceCall.succeeded()) {
        routingContext.response()
          .putHeader("Content-Type", "application/json")
          .end(new JsonArray(serviceCall.result().stream().map(JsonObject::mapFrom).collect(Collectors.toList())).encode());
      } else {
        LOG.error("Error saving pollSource", serviceCall.cause());
        routingContext.response().setStatusCode(500).end();
      }
    });
  }

  void saveClient(final RoutingContext routingContext) {

    final JsonObject bodyAsJson = routingContext.getBodyAsJson();
    this.clientService.save(bodyAsJson.mapTo(Client.class), handler -> {
      if (handler.succeeded()) {
        LOG.debug("Client saved : {}", JsonObject.mapFrom(handler.result()));
        routingContext.response().putHeader("Content-Type", "application/json")
          .setStatusCode(201)
          .end(JsonObject.mapFrom(handler.result()).toString());
      } else {
        LOG.error("Error saving client", handler.cause());
        routingContext.response().setStatusCode(500).end();
      }
    });
  }

  void deleteClient(RoutingContext routingContext) {
    this.clientService.delete(routingContext.pathParam("id"), result -> {
      if (result.succeeded()) {
        routingContext.response()
          .setStatusCode(200).end();
      } else {
        LOG.error("Error deleting pollSource", result.cause());
        routingContext.response().setStatusCode(500).end();
      }
    });
  }

  void getClient(final RoutingContext routingContext) {
    this.clientService.get(routingContext.pathParam("id"), result -> {
      if (result.succeeded()) {
        routingContext.response()
          .putHeader("Content-Type", "application/json")
          .end(JsonObject.mapFrom(result.result()).encode());
      } else {
        LOG.error("Error saving client", result.cause());
        routingContext.response().setStatusCode(500).end();
      }
    });
  }

  void getAllClients(final RoutingContext routingContext) {
    this.clientService.getAll(serviceCall -> {
      if (serviceCall.succeeded()) {
        routingContext.response()
          .putHeader("Content-Type", "application/json")
          .end(new JsonArray(serviceCall.result().stream().map(JsonObject::mapFrom).collect(Collectors.toList())).encode());
      } else {
        LOG.error("Error saving client", serviceCall.cause());
        routingContext.response().setStatusCode(500).end();
      }
    });
  }

  void ping(final RoutingContext rc) {
    rc.response().putHeader("Content-Type", "plain/text").end("OK");
  }


}

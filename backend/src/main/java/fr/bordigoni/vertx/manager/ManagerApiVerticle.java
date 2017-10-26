package fr.bordigoni.vertx.manager;

import fr.bordigoni.vertx.manager.db.client.ClientService;
import fr.bordigoni.vertx.manager.db.pollsource.PollSourceService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by benoit on 17/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
public class ManagerApiVerticle extends AbstractVerticle {


  public static final String HTTP_PORT = "http.port";
  private Logger logger;

  @Override
  public void start() throws Exception {

    this.logger = LoggerFactory.getLogger(ManagerApiVerticle.class);

    PollSourceService pollSourceService = PollSourceService.createProxy(this.vertx);
    ClientService clientService = ClientService.createProxy(this.vertx);

    final ManagerRoutesHandlers managerRoutesHandlers = new ManagerRoutesHandlers(pollSourceService, clientService);

    final Router router = Router.router(this.vertx);

    router.route().handler(
      CorsHandler.create("*")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.DELETE)
        .allowedMethod(HttpMethod.OPTIONS)
        .allowedHeader("content-type")
    );

    router.post().handler(BodyHandler.create());
    router.get("/util/ping").handler(managerRoutesHandlers::ping);

    // poll sources
    router.post("/pollsource").handler(managerRoutesHandlers::savePollSource);
    router.get("/pollsource").handler(managerRoutesHandlers::getAllPollSources);
    router.get("/pollsource/:id").handler(managerRoutesHandlers::getPollSource);
    router.delete("/pollsource/:id").handler(managerRoutesHandlers::deletePollSource);

    // poll clients
    router.post("/client").handler(managerRoutesHandlers::saveClient);
    router.get("/client").handler(managerRoutesHandlers::getAllClients);
    router.get("/client/:id").handler(managerRoutesHandlers::getClient);
    router.delete("/client/:id").handler(managerRoutesHandlers::deleteClient);


    Integer port = config().getInteger(HTTP_PORT, 8080);
    this.vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(port, "localhost", ar -> {
        if (ar.succeeded()) {
          this.logger.info("Vertical started on port : {}", port);
        }
      });
  }


}

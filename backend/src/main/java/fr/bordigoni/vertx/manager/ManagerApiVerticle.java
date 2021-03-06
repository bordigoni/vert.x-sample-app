package fr.bordigoni.vertx.manager;

import fr.bordigoni.vertx.manager.db.client.ClientService;
import fr.bordigoni.vertx.manager.db.pollsource.PollSourceService;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
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
  private PollSourceService pollSourceService;
  private ClientService clientService;

  @Override
  public void start() throws Exception {


    this.logger = LoggerFactory.getLogger(ManagerApiVerticle.class);

    this.pollSourceService = PollSourceService.createProxy(this.vertx);
    this.clientService = ClientService.createProxy(this.vertx);

    final ManagerRoutesHandlers managerRoutesHandlers = new ManagerRoutesHandlers(this.pollSourceService, this.clientService);

    final Router router = Router.router(this.vertx);
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

    this.vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(config().getInteger(HTTP_PORT), "localhost", ar -> {
        if (ar.succeeded()) {
          this.logger.info("Vertical started on port : {}", config().getInteger(HTTP_PORT));
        }
      });


  }


}

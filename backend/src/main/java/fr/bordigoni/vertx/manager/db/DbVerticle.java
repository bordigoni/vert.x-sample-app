package fr.bordigoni.vertx.manager.db;

import fr.bordigoni.vertx.manager.db.client.ClientService;
import fr.bordigoni.vertx.manager.db.pollsource.PollSourceService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by benoit on 18/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
public class DbVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(DbVerticle.class);


  public static final String CONFIG_DB_URL = "db.url";
  public static final String CONFIG_DB_USER = "db.user";
  public static final String CONFIG_DB_PASSWORD = "db.password";
  public static final String CONFIG_DB_DRIVER_CLASS = "db.driver.class";
  public static final String CONFIG_DB_MAX_POOL_SIZE = "db.max_pool_size";

  private JDBCClient dbClient;

  @Override
  public void start(final Future<Void> verticleStart) throws Exception {

    this.dbClient = JDBCClient.createShared(this.vertx, new JsonObject()
        .put("url", config().getString(CONFIG_DB_URL, "jdbc:derby:db;create=true"))
        .put("user", config().getString(CONFIG_DB_USER, "root"))
        .put("password", config().getString(CONFIG_DB_PASSWORD, "root"))
        .put("driver_class", config().getString(CONFIG_DB_DRIVER_CLASS, "org.apache.derby.jdbc.EmbeddedDriver"))
        .put("max_pool_size", config().getInteger(CONFIG_DB_MAX_POOL_SIZE, 30)),
      "manager_DS");

    final Future<String> pollSourceServiceCreation = Future.future();

    PollSourceService.createService(this.dbClient, serviceCreation -> {
      if (serviceCreation.succeeded()) {
        ProxyHelper.registerService(PollSourceService.class, this.vertx, serviceCreation.result(), PollSourceService.NAME);
        pollSourceServiceCreation.complete();
      } else {
        pollSourceServiceCreation.fail(serviceCreation.cause());
      }
    });

    pollSourceServiceCreation.compose(id -> {

      Future<String> clientServiceCreation = Future.future();

      ClientService.createService(this.dbClient, serviceCreation -> {
        if (serviceCreation.succeeded()) {
          ProxyHelper.registerService(ClientService.class, this.vertx, serviceCreation.result(), ClientService.NAME);
          clientServiceCreation.complete();
        } else {
          clientServiceCreation.fail(serviceCreation.cause());
        }

      });

      return clientServiceCreation;


    }).setHandler(handler -> {
      if (handler.succeeded()) verticleStart.complete();
      else verticleStart.fail(handler.cause());
    });


  }


  @Override
  public void stop() throws Exception {
    if (this.dbClient != null) {
      this.dbClient.close();
    }
  }
}

package fr.bordigoni.vertx.manager;

import fr.bordigoni.vertx.manager.db.DbVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by benoit on 20/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
public class ManagerVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(ManagerVerticle.class);
  private static final String SYS_PROP_CONFIG = "config";

  public static final void main(String... args) {


    Vertx.vertx().deployVerticle(new ManagerVerticle(), ar->{
      if(ar.succeeded()) {
        LOG.info("OK");
      } else {
        ar.cause().printStackTrace(System.err);
      }
    });
  }

  @Override
  public void start(final Future<Void> startFuture) throws Exception {


    String configPath = System.getProperty(SYS_PROP_CONFIG);

    Future<DeploymentOptions> getConfig = Future.future();

    if (configPath != null) {
      ConfigRetriever configRetriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
          new ConfigStoreOptions()
            .setType("file")
            .setConfig(new JsonObject().put("path", configPath))));

      configRetriever.getConfig(handler -> {
        final DeploymentOptions options = new DeploymentOptions();
        if (handler.succeeded()) {
          options.setConfig(handler.result());
          getConfig.complete(options);
        } else {
          getConfig.fail(handler.cause());
        }
      });

    } else {
      LOG.warn("Could not find system property '{}'. Add -D{}=path/to/config to your java command, default configuration is applied", SYS_PROP_CONFIG, SYS_PROP_CONFIG);
      getConfig.complete(new DeploymentOptions());
    }


    getConfig.compose(options -> this.vertx.deployVerticle(DbVerticle.class.getName(), options, dbDeploy -> {
      if (dbDeploy.succeeded()) {
        LOG.info("Verticle deployed with id : {}", dbDeploy.result());
        this.vertx.deployVerticle(ManagerApiVerticle.class.getName(), apiDeploy -> {
          if (apiDeploy.succeeded()) {
            LOG.info("Verticle deployed with id : {}", apiDeploy.result());
            startFuture.complete();
          } else {
            startFuture.fail(apiDeploy.cause());
          }
        });
      } else {
        startFuture.fail(dbDeploy.cause());
      }
    }), Future.succeededFuture());


  }
}

package fr.bordigoni.vertx.manager;

import fr.bordigoni.vertx.manager.db.DbVerticle;
import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ManagerVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(ManagerVerticle.class);
//  private static final String SYS_PROP_CONFIG = "config";

  public static void main(String... args) {

    Vertx.vertx().rxDeployVerticle(ManagerVerticle.class.getName())
      .subscribe(id ->
        LOG.info("OK"), err -> LOG.error("Unable to deploy verticle : ", err));

  }


  @Override
  public void start(Future<Void> startFuture) throws Exception {


//    String configPath = System.getProperty(SYS_PROP_CONFIG);
//    Single<DeploymentOptions> options$;
//
//    if (configPath != null) {
//      ConfigRetriever configRetriever = ConfigRetriever.create(vertx,
//        new ConfigRetrieverOptions().addStore(
//          new ConfigStoreOptions()
//            .setType("file")
//            .setConfig(new JsonObject().put("path", configPath))));
//
//      options$ = configRetriever.rxGetConfig()
//        .map(config -> new DeploymentOptions().setConfig(config));
//    } else {
//      options$ = Single.just(new DeploymentOptions());
//      LOG.warn("Cloud not config using -D{}=/path/to/config.json. Property not set ?", SYS_PROP_CONFIG);
//    }


    this.vertx
      .rxDeployVerticle(DbVerticle.class.getName()).doOnSuccess(id -> LOG.info("DbVerticle deployed with id {}", id))
      .flatMap(options -> vertx.rxDeployVerticle(ManagerApiVerticle.class.getName()).doOnSuccess(id -> LOG.info("ApiVerticle deployed with id {}", id)))
      .subscribe(id -> {
        LOG.info("ManagerVerticle deployed with id : {}", id);
        startFuture.complete();
      }, startFuture::fail);


  }
}

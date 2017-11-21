package fr.bordigoni.vertx;

import fr.bordigoni.vertx.manager.ManagerVerticle;
import fr.bordigoni.vertx.poller.PollerVerticle;
import io.vertx.rxjava.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by benoit on 21/11/2017.
 */
public class Backend {
  private static final Logger logger = LoggerFactory.getLogger(Backend.class);

  public static void main(String... args) {

    Vertx vertx = Vertx.vertx();
    vertx.rxDeployVerticle(ManagerVerticle.class.getName())
      .flatMap(id -> vertx.rxDeployVerticle(PollerVerticle.class.getName()).doOnSuccess(ok -> logger.info("Poller deployed! : " + ok)))
      .subscribe(
        ok -> logger.info("We are all good"),
        e -> logger.error("Ouch", e)
      );

  }

}

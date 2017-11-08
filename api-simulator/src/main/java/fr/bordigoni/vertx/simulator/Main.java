package fr.bordigoni.vertx.simulator;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by benoit on 08/11/2017.
 */
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new SimulatorVerticle(), ar -> {
      if (ar.succeeded()) {
        logger.info("Api simulator is in motion");
      } else {
        logger.error("Something went wrong", ar.cause());
      }
    });

  }

}

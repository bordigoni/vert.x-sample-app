package fr.bordigoni.vertx.simulator;

import io.vertx.rxjava.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by benoit on 08/11/2017.
 */
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {

    Vertx.vertx().rxDeployVerticle(SimulatorVerticle.class.getName())
      .subscribe(id -> logger.info("Api simulator is in motion"), err -> logger.error("Something went wrong", err));

  }

}

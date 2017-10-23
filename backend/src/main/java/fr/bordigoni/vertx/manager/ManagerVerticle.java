package fr.bordigoni.vertx.manager;

import fr.bordigoni.vertx.manager.db.DbVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * Created by benoit on 20/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
public class ManagerVerticle extends AbstractVerticle {

  public static final void main(String... args) {
    Vertx.vertx().deployVerticle(new ManagerVerticle(), ar->{
      if(ar.succeeded()) {
        System.out.println("OK");
      } else {
        ar.cause().printStackTrace(System.err);
      }
    });
  }

  @Override
  public void start(final Future<Void> startFuture) throws Exception {

    final Future<String> dbDeploy = Future.future();

    this.vertx.deployVerticle(DbVerticle.class.getName(), deploy -> dbDeploy.completer());

    dbDeploy.compose(managerDeploy -> {
      final Future<String> apiDeploy = Future.future();
      this.vertx.deployVerticle(ManagerApiVerticle.class.getName(), apiDeploy.completer());
      return apiDeploy;
    }).setHandler(ar -> {
      if (ar.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });


  }
}

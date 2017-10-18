package fr.bordigoni.vertx.manager.db.pollsource;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.jdbc.JDBCClient;

/**
 * Created by benoit on 17/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
@ProxyGen
public interface PollSourceService {

  @Fluent
  PollSourceService get(String id, Handler<AsyncResult<PollSource>> resultHandler);

  @Fluent
  PollSourceService save(PollSource pollSource, Handler<AsyncResult<PollSource>> saveHandler);

  static PollSourceService createService(final Vertx vertx, final JDBCClient dbClient, final Handler<AsyncResult<PollSourceService>> handler) {
    return new PollSourceServiceImpl(vertx, dbClient, handler);
  }

  static PollSourceService createProxy(final Vertx vertx) {
    return new PollSourceServiceVertxEBProxy(vertx, PollSourceService.class.getName());
  }

}

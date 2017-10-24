package fr.bordigoni.vertx.manager.db.pollsource;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.List;

/**
 * Created by benoit on 17/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
@ProxyGen
public interface PollSourceService {

  String NAME = PollSourceService.class.getSimpleName();

  @Fluent
  PollSourceService get(String id, Handler<AsyncResult<PollSource>> resultHandler);

  @Fluent
  PollSourceService save(PollSource pollSource, Handler<AsyncResult<PollSource>> saveHandler);

  @Fluent
  PollSourceService delete(String id, Handler<AsyncResult<Void>> deleteHandler);

  @Fluent
  PollSourceService getAll( Handler<AsyncResult<List<PollSource>>> getAllHandler);


  static PollSourceService createService(final JDBCClient dbClient, final Handler<AsyncResult<PollSourceService>> handler) {
    return new PollSourceServiceImpl(dbClient, handler);
  }

  static PollSourceService createProxy(final Vertx vertx) {
    return new PollSourceServiceVertxEBProxy(vertx, PollSourceService.NAME);
  }
}

package fr.bordigoni.vertx.manager.db.client;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.List;


/**
 * Created by benoit on 20/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
@ProxyGen
public interface ClientService {

  String NAME = ClientService.class.getSimpleName();

  @Fluent
  ClientService save(Client client, Handler<AsyncResult<Client>> handler);

  @Fluent
  ClientService update(Client client, Handler<AsyncResult<Void>> handler);

  @Fluent
  ClientService get(String id, Handler<AsyncResult<Client>> handler);

  @Fluent
  ClientService getAll(Handler<AsyncResult<List<Client>>> handler);

  @Fluent
  ClientService delete(String id, Handler<AsyncResult<Void>> handler);


  static ClientService createService(JDBCClient dbClient, Handler<AsyncResult<ClientService>> handler) {
    return new ClientServiceImpl(dbClient, handler);
  }

  static ClientService createProxy(Vertx vertx) {
    return new ClientServiceVertxEBProxy(vertx, NAME);
  }



}

package fr.bordigoni.vertx.manager.db.client;

import fr.bordigoni.vertx.manager.db.AbstractDbService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.List;
import java.util.UUID;

/**
 * Created by benoit on 20/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
public class ClientServiceImpl extends AbstractDbService<ClientService> implements ClientService {

  private static final String TABLE_NAME = "CLIENT";

  private static final String CREATE_TABLE = "create table " + TABLE_NAME + " (" +
    "ID varchar(36)," +
    "NAME varchar(255) not null," +
    "EMAIL varchar(255) not null," +
    "PASSWORD varchar(255) not null," +
    "primary key (ID))";
  private static final String SQL_SAVE = "insert into " + TABLE_NAME + " (ID, NAME, EMAIL, PASSWORD) values (?,?,?, ?)";
  private static final String SQL_GET_ONE = "select * from " + TABLE_NAME + " where ID=?";
  private static final String SQL_GET_ALL = "select * from " + TABLE_NAME;

  public ClientServiceImpl(final JDBCClient dbClient, final Handler<AsyncResult<ClientService>> handler) {
    super(dbClient, TABLE_NAME, CREATE_TABLE, handler);
  }

  @Override
  public ClientService save(Client client, Handler<AsyncResult<Client>> saveHandler) {

    client.setId(UUID.randomUUID().toString());

    super.save(
      client,
      SQL_SAVE,
      new JsonArray()
        .add(client.getId())
        .add(client.getName())
        .add(client.getEmail())
        .add(client.getPassword()),
      saveHandler
    );

    return this;
  }

  @Override
  public ClientService get(String id, Handler<AsyncResult<Client>> getHandler) {

    super.get(SQL_GET_ONE, id, Client::new, getHandler);

    return this;
  }


  @Override
  public ClientService getAll(Handler<AsyncResult<List<Client>>> getAllHandler) {
    super.getAll(SQL_GET_ALL, Client::new, getAllHandler);
    return this;
  }


}

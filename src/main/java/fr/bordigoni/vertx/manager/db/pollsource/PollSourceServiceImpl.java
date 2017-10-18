package fr.bordigoni.vertx.manager.db.pollsource;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;
import java.util.UUID;

/**
 * Created by benoit on 17/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */

public class PollSourceServiceImpl implements PollSourceService {


  private static final String CREATE_TABLE = "create table POLL_SOURCE (" +
    "ID varchar(36)," +
    "URL varchar(512) not null," +
    "DELAY integer not null," +
    "primary key (ID))";
  private static final String SQL_SAVE = "insert into POLL_SOURCE (ID, URL, DELAY) values (?,?,?)";
  private static final String SQL_GET_ONE = "select * from POLL_SOURCE where ID=?";

  private final Vertx vertx;
  private final JDBCClient dbClient;


  public PollSourceServiceImpl(final Vertx vertx, final JDBCClient dbClient, final Handler<AsyncResult<PollSourceService>> handler) {
    this.vertx = vertx;
    this.dbClient = dbClient;

    this.dbClient.getConnection(getConnection -> {
      if (getConnection.succeeded()) {
        final SQLConnection sqlConnection = getConnection.result();

        sqlConnection.query("select * from SYS.SYSTABLES where TABLETYPE='T' and TABLENAME='POLL_SOURCE'", checkTable -> {
          if (checkTable.succeeded()) {
            if (checkTable.result().getRows().isEmpty()) {
              sqlConnection.execute(CREATE_TABLE, createTable -> {
                if (createTable.succeeded()) {
                  handler.handle(Future.succeededFuture(this));
                } else {
                  handler.handle(Future.failedFuture(createTable.cause()));
                }
              });
            } else {
              handler.handle(Future.succeededFuture(this));
            }
            // table is already here
          } else {
            handler.handle(Future.failedFuture(checkTable.cause()));
          }

        });


      } else {
        handler.handle(Future.failedFuture(getConnection.cause()));
      }
    });


  }

  @Override
  public PollSourceService save(final PollSource pollSource, final Handler<AsyncResult<PollSource>> saveHandler) {
    pollSource.setId(UUID.randomUUID().toString());
    this.dbClient.getConnection(getConnection -> {
      if (getConnection.succeeded()) {
        getConnection.result().updateWithParams(SQL_SAVE, new JsonArray()
          .add(pollSource.getId())
          .add(pollSource.getUrl())
          .add(pollSource.getDelay()), insert -> {
          if (insert.succeeded()) {
            saveHandler.handle(Future.succeededFuture(pollSource));
          } else {
            saveHandler.handle(Future.failedFuture(insert.cause()));
          }
        });
      }
    });
    return this;
  }


  public PollSourceService get(final String id, final Handler<AsyncResult<PollSource>> getHandler) {
    this.dbClient.getConnection(connection -> {
      if (connection.succeeded()) {
        connection.result().queryWithParams(SQL_GET_ONE, new JsonArray().add(id), select -> {
          if (select.succeeded()) {
            final List<JsonObject> rows = select.result().getRows();
            if (rows.isEmpty()) {
              getHandler.handle(Future.succeededFuture(null));
            } else if (rows.size() == 1) {
              final PollSource pollSource = new PollSource();
              pollSource.setId(rows.get(0).getString("ID"));
              pollSource.setUrl(rows.get(0).getString("URL"));
              pollSource.setDelay(rows.get(0).getInteger("DELAY"));
              getHandler.handle(Future.succeededFuture(pollSource));
            } else {
              getHandler.handle(Future.failedFuture("Many result (" + rows.size() + ") where found where only one was expected."));
            }
          } else {
            getHandler.handle(Future.failedFuture(select.cause()));
          }
        });
      } else {
        getHandler.handle(Future.failedFuture(connection.cause()));
      }
    });
    return this;
  }


}

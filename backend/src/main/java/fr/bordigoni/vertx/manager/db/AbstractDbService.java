package fr.bordigoni.vertx.manager.db;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;


/**
 * Created by benoit on 20/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
public abstract class AbstractDbService<T> {
  private final JDBCClient dbClient;
  private static final Logger LOG = LoggerFactory.getLogger(AbstractDbService.class);


  public AbstractDbService(JDBCClient dbClient, String tableName, String tableCreationScript, Handler<AsyncResult<T>> handler) {
    this.dbClient = dbClient;
    this.dbClient.getConnection(getConnection -> {
      if (getConnection.succeeded()) {

        final SQLConnection sqlConnection = getConnection.result();
        LOG.info("Connection to database established.");

        sqlConnection.query("select * from SYS.SYSTABLES where TABLETYPE='T' and TABLENAME='" + tableName + "'", checkTable -> {
          if (checkTable.succeeded()) {
            if (checkTable.result().getRows().isEmpty()) {
              LOG.info("No table named "+tableName+" found. Table will be created.");
              sqlConnection.execute(tableCreationScript, createTable -> {
                if (createTable.succeeded()) {
                  handler.handle(Future.succeededFuture((T)this));
                } else {
                  handler.handle(Future.failedFuture(createTable.cause()));
                }
              });
            } else {
              handler.handle(Future.succeededFuture((T)this));
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


  protected final <R> void save(final R entity, final String SQL, JsonArray sqlParams, Handler<AsyncResult<R>> saveHandler) {

    this.dbClient.getConnection(getConnection -> {
      if (getConnection.succeeded()) {
        getConnection.result().updateWithParams(SQL, sqlParams, insert -> {
          if (insert.succeeded()) {
            saveHandler.handle(Future.succeededFuture(entity));
          } else {
            saveHandler.handle(Future.failedFuture(insert.cause()));
          }
        });
      }
    });


  }

protected final <R> void get(String SQL, Object id, ResultRowMapper<R> rowMapper, Handler<AsyncResult<R>> getHandler) {
  this.dbClient.getConnection(connection -> {
    if (connection.succeeded()) {
      connection.result().queryWithParams(SQL, new JsonArray().add(id), select -> {
        if (select.succeeded()) {
          final List<JsonObject> rows = select.result().getRows();
          if (rows.isEmpty()) {
            getHandler.handle(Future.succeededFuture(null));
          } else if (rows.size() == 1) {
            getHandler.handle(Future.succeededFuture(rowMapper.map(rows.get(0))));
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
}

  protected final <R> void getAll(final String SQL, final ResultRowMapper<R> rowMapper, final Handler<AsyncResult<List<R>>> getAllHandler) {
    this.dbClient.getConnection(connection -> {
      if (connection.succeeded()) {
        connection.result().query(SQL, select -> {
          if (select.succeeded()) {
            final List<JsonObject> rows = select.result().getRows();
            getAllHandler.handle(Future.succeededFuture(rows.stream().map(rowMapper::map).collect(toList())));
          } else {
            getAllHandler.handle(Future.failedFuture(select.cause()));
          }
        });
      } else {
        getAllHandler.handle(Future.failedFuture(connection.cause()));
      }
    });
  }

  protected final void delete(String SQL, String id, Handler<AsyncResult<Void>> deleteHandler) {
    this.dbClient.getConnection(getConnection -> {
      if (getConnection.succeeded()) {
        getConnection.result().updateWithParams(SQL, new JsonArray().add(id), delete -> {
          if (delete.succeeded()) {
            deleteHandler.handle(Future.succeededFuture());
          } else {
            deleteHandler.handle(Future.failedFuture(delete.cause()));
          }
        });
      }
    });

  }
}

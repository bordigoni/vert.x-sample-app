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
 * Allow a user to execute basic queries limiting the amount of boilerplate code.
 */
public abstract class AbstractDbService<S> {
  private final JDBCClient dbClient;
  private static final Logger LOG = LoggerFactory.getLogger(AbstractDbService.class);


  public AbstractDbService(JDBCClient dbClient, String tableName, String tableCreationScript, Handler<AsyncResult<S>> handler) {
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
                  handler.handle(Future.succeededFuture((S) this));
                } else {
                  handler.handle(Future.failedFuture(createTable.cause()));
                }
              });
            } else {
              handler.handle(Future.succeededFuture((S) this));
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


  /**
   * Save the entity into the database.
   *
   * @param entity      the entity that will be return to the caller when insert is done
   * @param SQL         the insert SQL String
   * @param sqlParams   param to be injected into the insert query
   * @param saveHandler the handle that will carry the result of the insertion
   * @param <T>         the entity type (for type inference logic)
   */
  protected final <T> void save(final T entity, final String SQL, JsonArray sqlParams, Handler<AsyncResult<T>> saveHandler) {

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

  /**
   * Save the entity into the database.
   *
   * @param entity      the entity that will be return to the caller when insert is done
   * @param SQL         the update SQL String
   * @param sqlParams   param to be injected into the insert query
   * @param saveHandler the handle that will carry the result of the update
   * @param <T>         the entity type (for type inference logic)
   */
  protected final <T> void update(final T entity, final String SQL, JsonArray sqlParams, Handler<AsyncResult<Void>> saveHandler) {

    this.dbClient.getConnection(getConnection -> {
      if (getConnection.succeeded()) {
        getConnection.result().updateWithParams(SQL, sqlParams, insert -> {
          if (insert.succeeded()) {
            saveHandler.handle(Future.succeededFuture());
          } else {
            saveHandler.handle(Future.failedFuture(insert.cause()));
          }
        });
      }
    });

  }

  /**
   * Get an entity from the database. The result wil fail if more than one row is found but will <b>succeed with <code>null</code> if no row is found</b>
   *
   * @param SQL        the select to be executed
   * @param id         the primary key value
   * @param rowMapper  a {@link ResultRowMapper} to map a row as a {@link JsonObject} to an object. Can be a lambda expression
   * @param getHandler the handle that will carry the result of the select
   * @param <T>        the entity type
   */
  protected final <T> void get(String SQL, Object id, ResultRowMapper<T> rowMapper, Handler<AsyncResult<T>> getHandler) {
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

  /**
   * Will select a collection of row an map to an object type
   *
   * @param SQL           the select query
   * @param rowMapper     a {@link ResultRowMapper} to map one row as a {@link JsonObject} to an object. Will be called one every row. Can be a lambda expression.
   * @param getAllHandler the handle that will carry the result of the select as a list (never null) of object
   * @param <T>           type of Object contained in the list
   */
  protected final <T> void getAll(final String SQL, final ResultRowMapper<T> rowMapper, final Handler<AsyncResult<List<T>>> getAllHandler) {
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

  /**
   * Will delete a row according the SQL passed and a primary key
   *
   * @param SQL           delete clause on a primary key
   * @param id            primary key value
   * @param deleteHandler the handle that will carry the success of failure of the delete
   */
  protected final void delete(String SQL, Object id, Handler<AsyncResult<Void>> deleteHandler) {
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

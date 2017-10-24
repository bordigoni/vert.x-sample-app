package fr.bordigoni.vertx.manager.db.pollsource;

import fr.bordigoni.vertx.manager.db.AbstractDbService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.List;
import java.util.UUID;

/**
 * Created by benoit on 17/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */

public class PollSourceServiceImpl extends AbstractDbService<PollSourceService> implements PollSourceService {


  private static final String TABLE_NAME = "POLLSOURCE";
  private static final String CREATE_TABLE = "create table " + TABLE_NAME + " (" +
    "ID varchar(36)," +
    "URL varchar(512) not null," +
    "DELAY integer not null," +
    "primary key (ID))";
  private static final String SQL_SAVE = "insert into " + TABLE_NAME + " (ID, URL, DELAY) values (?,?,?)";
  private static final String SQL_GET_ONE = "select * from " + TABLE_NAME + " where ID=?";
  private static final String SQL_DELETE = "delete from " + TABLE_NAME + " where ID=?";
  private static final String SQL_GET_ALL = "select * from " + TABLE_NAME ;


  PollSourceServiceImpl(final JDBCClient dbClient, final Handler<AsyncResult<PollSourceService>> handler) {
    super(dbClient, TABLE_NAME, CREATE_TABLE, handler);

  }

  @Override
  public PollSourceService save(final PollSource pollSource, final Handler<AsyncResult<PollSource>> saveHandler) {
    pollSource.setId(UUID.randomUUID().toString());

    super.save(pollSource, SQL_SAVE, new JsonArray()
      .add(pollSource.getId())
      .add(pollSource.getUrl())
      .add(pollSource.getDelay()), saveHandler);

    return this;
  }

  @Override
  public PollSourceService delete(String id, Handler<AsyncResult<Void>> deleteHandler) {
    super.delete(SQL_DELETE, id, deleteHandler);
    return this;
  }

  @Override
  public PollSourceService get(final String id, final Handler<AsyncResult<PollSource>> getHandler) {
    super.get(SQL_GET_ONE, id, PollSource::new, getHandler);
    return this;
  }

  @Override
  public PollSourceService getAll(final Handler<AsyncResult<List<PollSource>>> getAllHandler) {
     super.getAll(SQL_GET_ALL, PollSource::new, getAllHandler);
     return this;
  }



}

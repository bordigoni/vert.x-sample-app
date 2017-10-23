package fr.bordigoni.vertx.manager.db;

import io.vertx.core.json.JsonObject;

/**
 * An interface to used as a lamdba expression to map a {@link JsonObject} on a POJO.
 * @param <T> the type be returned.
 */
@FunctionalInterface
public interface ResultRowMapper<T> {

  /**
   * A simple method that allow the user to implement mapping logic between a {@link JsonObject} and a POJO.
   * @param object the JSON Object
   * @return a POJO
   */
  T map(JsonObject object);

}

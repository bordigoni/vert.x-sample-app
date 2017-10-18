package fr.bordigoni.vertx.manager.db.pollsource;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonObject;

/**
 * Created by benoit on 17/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
@DataObject
public class PollSource {

  @Nullable
  private String id;
  private String url;
  private Integer delay;

  public PollSource() {

  }

  public PollSource(final String url, final int delay) {
    this.url = url;
    this.delay = delay;
  }

  public PollSource(final JsonObject jsonObject) {
    final PollSource pollSource = jsonObject.mapTo(PollSource.class);
    this.id = pollSource.id;
    this.url = pollSource.url;
    this.delay = pollSource.delay;
  }

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }


  public String getId() {
    return this.id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getUrl() {
    return this.url;
  }

  public Integer getDelay() {
    return this.delay;
  }

  public void setDelay(final Integer delay) {
    this.delay = delay;
  }

  public void setUrl(final String url) {
    this.url = url;
  }
}

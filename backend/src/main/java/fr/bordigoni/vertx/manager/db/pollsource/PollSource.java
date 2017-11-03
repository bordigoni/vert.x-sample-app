package fr.bordigoni.vertx.manager.db.pollsource;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Created by benoit on 17/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */
@DataObject
public class PollSource {

  private String id;
  private String clientId;
  private String url;
  private Integer delay;

  public PollSource() {

  }

  public PollSource(final String clientId, final String url, final int delay) {
    this.clientId = clientId;
    this.url = url;
    this.delay = delay;
  }

  public PollSource(final JsonObject json) {
    this.id=json.getString("id",json.getString("ID"));
    this.clientId = json.getString("clientId", json.getString("CLIENT_ID"));
    this.url=json.getString("url",json.getString("URL"));
    this.delay=json.getInteger("delay",json.getInteger("DELAY"));
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

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
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

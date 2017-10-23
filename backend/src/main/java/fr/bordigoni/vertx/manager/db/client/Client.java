package fr.bordigoni.vertx.manager.db.client;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Created by benoit on 20/10/2017.
 * This file is the property of IEVA SAS only. This is not free to use code.
 * It is not allowed to use or modify the present file without IEVA authorization.
 */

@DataObject
public class Client{

  private String id;
  private String name;
  private String email;
  private String password;

  public Client() {

  }

  public Client(JsonObject json) {
    this.id=json.getString("id",json.getString("ID"));
    this.name=json.getString("name",json.getString("NAME"));
    this.email=json.getString("email",json.getString("EMAIL"));
    this.password=json.getString("password",json.getString("PASSWORD"));
  }

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Client client = (Client) o;

    if (id != null ? !id.equals(client.id) : client.id != null) return false;
    return email != null ? email.equals(client.email) : client.email == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (email != null ? email.hashCode() : 0);
    return result;
  }
}

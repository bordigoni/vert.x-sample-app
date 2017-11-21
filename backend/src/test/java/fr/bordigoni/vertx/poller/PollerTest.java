package fr.bordigoni.vertx.poller;

import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by benoit on 21/11/2017.
 */
public class PollerTest {

  @Test
  public void testObjectToPatch() {

    JsonObject jsonObject = new JsonObject()
      .put("code", "ABC")
      .put("value", 123);


    Poller poller = new Poller(Vertx.vertx(), null);

    {
      JsonNode jsonNode = poller.toPatch(jsonObject.encode());
      assertNotNull(jsonNode);
      JsonArray patch = poller.toJsonArray(jsonNode);
      assertEquals(1, patch.size());
      assertEquals("replace", patch.getJsonObject(0).getString("op"));
      assertEquals("/", patch.getJsonObject(0).getString("path"));
      assertEquals(jsonObject, patch.getJsonObject(0).getJsonObject("value"));
    }

    {
      jsonObject.put("value", 456);
      final JsonArray patch = poller.toJsonArray(poller.toPatch(jsonObject.encode()));
      assertEquals(1, patch.size());
      assertEquals("replace", patch.getJsonObject(0).getString("op"));
      assertEquals("/value", patch.getJsonObject(0).getString("path"));
      assertEquals(456, (long) patch.getJsonObject(0).getLong("value"));
    }

    {
      final JsonArray patch = poller.toJsonArray(poller.toPatch(jsonObject.encode()));
      assertEquals(0, patch.size());

    }
  }


}

package fr.bordigoni.vertx.poller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import fr.bordigoni.vertx.manager.db.pollsource.PollSource;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by benoit on 15/11/2017.
 */
class Poller {

  private static final Logger LOGGER = LoggerFactory.getLogger(Poller.class);

  private static final EnumSet<DiffFlags> FLAGS = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone();

  private final WebClient webClient;
  private final PollSource source;
  private final Vertx vertx;

  private JsonNode lastEntry;

  Poller(Vertx vertx, PollSource source) {
    this.vertx = vertx;
    this.webClient = WebClient.create(vertx);
    this.source = source;
  }

  /**
   * Poll from the source given in when build this object. Return an Obsevable of JSON patch as an JsonArray
   *
   * @return an Observable of successive JSON patch
   */
  Observable<JsonArray> poll() {

    LOGGER.info("Start polling from {} every {}ms", this.source.getUrl(), this.source.getDelay());

    try {
      URI uri = new URI(this.source.getUrl());
      return Observable.timer(this.source.getDelay(), TimeUnit.MILLISECONDS)
        // make it work the vert.x way
        .subscribeOn(RxHelper.scheduler(vertx))
        .map(t -> {
          return webClient.get(uri.getPort(), uri.getHost(), uri.getPath()).as(BodyCodec.string()).rxSend();
        })
        .flatMapSingle(s -> s.map(HttpResponse::body))
        .doOnEach(json -> LOGGER.info("Polled {} from {}", json, this.source.getUrl()))
        .map(this::toPatch)
        .filter(Objects::nonNull) // in case of mapping error
        .map(this::toJsonArray)
        .filter(json -> json.size() > 0); // in case of mapping error

    } catch (URISyntaxException e) {
      LOGGER.error(e.getMessage(), e);
      return Observable.empty();
    }


  }


  /**
   * Creates a patch to compare a node to the last one received
   *
   * @param entry the entry to compare to the last one received
   * @return differences between nodes
   */
  JsonNode toPatch(String entry) {

    try {
      final JsonNode newEntryNode = new ObjectMapper().readTree(entry);
      if (this.lastEntry == null) {
        this.lastEntry = new ObjectMapper().readTree("null");
      }
      JsonNode result = JsonDiff.asJson(this.lastEntry, newEntryNode, FLAGS);
      this.lastEntry = newEntryNode;
      return result;
    } catch (IOException e) {
      LOGGER.warn("Cannot decode json, return null instead", e);
      return null;
    }

  }

  /**
   * Converts a {@link JsonNode} to a {@link JsonObject}
   *
   * @param node the node to convert
   * @return the node as a JsonObject
   */
  JsonArray toJsonArray(JsonNode node) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      final Object obj = objectMapper.treeToValue(node, Object[].class);
      final String json = objectMapper.writeValueAsString(obj);
      return new JsonArray(json);
    } catch (JsonProcessingException e) {
      LOGGER.warn("Cannot encode json, return an empty object instead", e);
      return new JsonArray();
    }
  }

}

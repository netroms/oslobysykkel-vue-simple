

package no.inarctica.oslobysykkel;

import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import java.io.StringReader;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;


public class StationApiProxyService implements Service {

  private static final Logger logger = Logger.getLogger(StationApiProxyService.class.getName());

  // Cache stations for 10 seconds
  private static final long MAX_CACHE_TIME = 10000L;
  private static final String STATION_CACHE_KEY = "STATION_CACHE_KEY";


  public static class CachedMapEntry {

    long created;
    Map<Integer, Map<String, String>> content;

    CachedMapEntry(Map<Integer, Map<String, String>> content) {
      this.created = Instant.now().toEpochMilli();
      this.content = content;
    }
  }

  private static final Map<String, CachedMapEntry> stationCache = new ConcurrentHashMap<>();

  private final String apiKey;


  StationApiProxyService(Config config) {
    this.apiKey = config.get("app.api_key").asString().orElse("");
  }

  private void addCORSHeaders(ServerResponse serverResponse) {
    serverResponse.headers().add("Access-Control-Allow-Origin", "*");
    serverResponse.headers().add("Access-Control-Allow-Methods", "GET");
    serverResponse.headers().add("Access-Control-Allow-Headers", "client-identifier");
    serverResponse.headers().add("Access-Control-Expose-Headers", "access-control-allow-origin,access-control-allow-methods,access-control-allow-headers");
  }

  @Override
  public void update(Routing.Rules rules) {
    rules.get("/stativer", this::getStationsAndAvailability)
        .options("/stativer", this::handleOptions)
        .get("/status", this::getStatus)
        .options("/status", this::handleOptions);
  }

  private void getStatus(ServerRequest serverRequest, ServerResponse response) {
    addCORSHeaders(response);
    try {
      JsonObject json = getJsonFromUrl(RemoteApiUrls.HTTPS_OSLOBYSYKKEL_NO_API_V_1_STATUS);
      response.send(json);
    } catch (Exception e) {
      logger.severe("Failed to fetch oslobysykkel.no/api/v1/status; Exception=" + e.getMessage());
      response.send("error");
    }
  }

  private void handleOptions(ServerRequest serverRequest, ServerResponse serverResponse) {
    addCORSHeaders(serverResponse);
    serverResponse.send();
  }

  private void getStationsAndAvailability(ServerRequest request, ServerResponse response) {
    addCORSHeaders(response);
    try {
      JsonObjectBuilder responseJson = buildResponse(getAvailabilityMap(), getStationMap());
      response.send(responseJson.build());

    } catch (Exception e) {
      logger.severe("Failed to fetch remote API; Exception=" + e.getMessage());
      response.send("error");
    }
  }

  private static JsonObjectBuilder buildResponse(Map<Integer, JsonObject> availMap, Map<Integer, Map<String, String>> stationMap) {
    final JsonArrayBuilder stationJsonArray = Json.createArrayBuilder();
    stationMap.forEach((key, value) -> {
      String aStr = availMap.get(key).toString();
      JsonObjectBuilder sB = Json.createObjectBuilder();
      sB.add("id", key);
      sB.add("title", value.get("title"));
      JsonReader reader = Json.createReader(new StringReader(aStr));
      JsonObject avail = reader.readObject();
      sB.add("availability", avail);
      stationJsonArray.add(sB);
    });

    JsonObjectBuilder responseJson = Json.createObjectBuilder();
    responseJson.add("status", "OK");
    responseJson.add("stations", stationJsonArray);
    return responseJson;
  }

  Map<Integer, JsonObject> parseAvailStream(Stream<JsonValue> s) {
    return s.collect(Collectors.toMap(
        o -> o.asJsonObject().getInt("id"),
        o -> o.asJsonObject().getJsonObject("availability")
    ));
  }

  Map<Integer, Map<String, String>> parseStationsStream(Stream<JsonValue> s) {
    return s.collect(Collectors.toMap(
        o -> o.asJsonObject().getInt("id"),
        o -> Map.of("title", o.asJsonObject().getString("title"))
    ));
  }

  private Map<Integer, JsonObject> getAvailabilityMap() throws Exception {
    return parseAvailStream(getAvailabilityStream());
  }

  private Map<Integer, Map<String, String>> getStationMap() throws Exception {
    if (stationCache.containsKey(STATION_CACHE_KEY)) {
      CachedMapEntry cacheEntry = stationCache.get(STATION_CACHE_KEY);
      long elapsedCacheTime = Instant.now().toEpochMilli() - cacheEntry.created;
      if (elapsedCacheTime < MAX_CACHE_TIME) {
        return stationCache.get(STATION_CACHE_KEY).content;
      }
    }
    Map<Integer, Map<String, String>> fetchedMap = parseStationsStream(fetchStationStream());
    stationCache.put(STATION_CACHE_KEY, new CachedMapEntry(fetchedMap));
    return fetchedMap;
  }


  private Stream<JsonValue> fetchStationStream() throws Exception {
    return getStationsAsJsonValueStream(getJsonFromUrl(RemoteApiUrls.HTTPS_OSLOBYSYKKEL_NO_API_V_1_STATIONS));
  }

  private Stream<JsonValue> getAvailabilityStream() throws Exception {
    return getStationsAsJsonValueStream(getJsonFromUrl(RemoteApiUrls.HTTPS_OSLOBYSYKKEL_NO_API_V_1_STATIONS_AVAILABILITY));
  }

  Stream<JsonValue> getStationsAsJsonValueStream(JsonObject stations) {
    Iterable<JsonValue> iterable = () -> stations.getJsonArray("stations").iterator();
    return StreamSupport.stream(iterable.spliterator(), false);
  }

  private JsonObject getJsonFromUrl(String uri) throws Exception {
    return Json.createReader(new StringReader(HttpUtils.httpGET(uri, apiKey))).readObject();
  }
}

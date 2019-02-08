package no.inarctica.oslobysykkel;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.helidon.config.Config;
import java.io.StringReader;
import java.util.Map;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StationApiProxyServiceTest {

  public static final String STATIONS_JSON = "{  \"stations\": [{\"id\": 157,\"in_service\": true,\"title\": \"Nylandsveien\","
      + " \"subtitle\": \"mellom Norbygata og Urtegata\",\"number_of_locks\": 30,\"center\": {\"latitude\": 59.91562,\"longitude\": 10.762248},"
      + " \"bounds\": [{\"latitude\": 59.915418602160436,\"longitude\": 10.762068629264832},{\"latitude\": 59.91565254992276,"
      + "   \"longitude\": 10.762672126293182},{\"latitude\": 59.915807169665264,\"longitude\": 10.762433409690855},{"
      + "   \"latitude\": 59.91557994562126,\"longitude\": 10.761821866035461},{\"latitude\": 59.915418602160436,"
      + "    \"longitude\": 10.762068629264832 } ] }]}";
  public static final String AVAIL_JSON = "{\"stations\": [{\"id\": 177,\"availability\": {\"bikes\": 0,\"locks\": 29,\"overflow_capacity\": false}}]}";

  public static final String AVAIL_JSON_BROKEN = "{\"sXXXtations\": [{\"id\": 177,\"availability\": {\"bikes\": 0,\"locks\": 29,\"overflow_capacity\": false}}]}";

  @Test
  public void testStationParse() {
    Config config = Config.create();
    StationApiProxyService stationApiProxyService = new StationApiProxyService(config);

    JsonReader reader = Json.createReader(new StringReader(STATIONS_JSON));
    Stream<JsonValue> stationsAsJsonValueStream = stationApiProxyService.getStationsAsJsonValueStream(reader.readObject());
    Map<Integer, Map<String, String>> integerMapMap = stationApiProxyService.parseStationsStream(stationsAsJsonValueStream);

    integerMapMap.forEach((integer, stringStringMap) -> {
      Assertions.assertEquals("157", integer.toString(), "first id should be '157'");
    });
  }

  @Test
  public void testAvailabilityParse() {
    Config config = Config.create();
    StationApiProxyService stationApiProxyService = new StationApiProxyService(config);

    JsonReader reader = Json.createReader(new StringReader(AVAIL_JSON));
    Stream<JsonValue> stationsAsJsonValueStream = stationApiProxyService.getStationsAsJsonValueStream(reader.readObject());
    Map<Integer, JsonObject> integerJsonObjectMap = stationApiProxyService.parseAvailStream(stationsAsJsonValueStream);

    integerJsonObjectMap.forEach((integer, stringStringMap) -> {
      Assertions.assertEquals("177", integer.toString(), "first id should be '177'");
    });
  }

  @Test
  public void testParseBrokenAvailabilityJson() {
    Config config = Config.create();
    StationApiProxyService stationApiProxyService = new StationApiProxyService(config);

    JsonReader reader = Json.createReader(new StringReader(AVAIL_JSON_BROKEN));
    JsonObject stations = reader.readObject();

    assertThrows(java.lang.NullPointerException.class, () -> stationApiProxyService.getStationsAsJsonValueStream(stations));
  }
}
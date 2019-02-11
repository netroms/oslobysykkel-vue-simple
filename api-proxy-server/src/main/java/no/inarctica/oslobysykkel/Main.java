
package no.inarctica.oslobysykkel;

import io.helidon.config.Config;
import io.helidon.health.HealthSupport;
import io.helidon.health.checks.HealthChecks;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.json.JsonSupport;
import java.io.IOException;
import java.util.logging.LogManager;

public final class Main {

  /**
   * Cannot be instantiated.
   */
  private Main() {
  }

  /**
   * Application main entry point.
   *
   * @param args command line arguments.
   * @throws IOException if there are problems reading logging properties
   */
  public static void main(final String[] args) throws IOException {
    startServer();
  }

  /**
   * Start the server.
   *
   * @return the created {@link WebServer} instance
   * @throws IOException if there are problems reading logging properties
   */
  static WebServer startServer() throws IOException {

    // load logging configuration
    LogManager.getLogManager().readConfiguration(
        Main.class.getResourceAsStream("/logging.properties"));

    // By default this will pick up application.yaml from the classpath
    Config config = Config.create();

    // Get webserver config from the "server" section of application.yaml
    ServerConfiguration serverConfig =
        ServerConfiguration.create(config.get("server"));

    WebServer server = WebServer.create(serverConfig, createRouting(config));

    // Start the server and print some info.
    server.start().thenAccept(ws ->
        System.out.println("WEB server is up! http://localhost:" + ws.port() + "/api"));

    // Server threads are not daemon. NO need to block. Just react.
    server.whenShutdown().thenRun(() ->
        System.out.println("WEB server is DOWN. Good bye!"));

    return server;
  }

  /**
   * Creates new {@link Routing}.
   *
   * @param config configuration of this server
   * @return routing configured with JSON support, a health check, and a service
   */
  private static Routing createRouting(Config config) {
    HealthSupport health = HealthSupport.builder()
        .add(HealthChecks.healthChecks())
        .add(new RemoteApiHealthCheck(config.get("app.api_key").asString().orElse("")))
        .build();

    return Routing.builder()
        .register(JsonSupport.create())
        .register(health)                   // Health at "/health"
        .register(MetricsSupport.create())  // Metrics at "/metrics"
        .register("/api", new StationApiProxyService(config))
        .build();
  }

}

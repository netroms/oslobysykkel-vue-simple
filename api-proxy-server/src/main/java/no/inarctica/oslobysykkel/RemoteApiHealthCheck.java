package no.inarctica.oslobysykkel;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

public class RemoteApiHealthCheck implements HealthCheck {

  private String apiKey;

  public RemoteApiHealthCheck(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public HealthCheckResponse call() {
    boolean isUp = false;
    String status;
    try {
      status = HttpUtils.httpGET("https://oslobysykkel.no/api/v1/status/", apiKey);
      isUp = true;
    } catch (Exception e) {
      status = e.getMessage();
    }

    return HealthCheckResponse.named("remoteApiStatus")
        .state(isUp)
        .withData("status", status)
        .build();
  }
}

package no.inarctica.oslobysykkel;

import static no.inarctica.oslobysykkel.RemoteApiUrls.HTTPS_OSLOBYSYKKEL_NO_API_V_1_STATUS;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

/**
 * Simple health check to check if oslobysykkel api is responding.
 * Just calls the status endpoint and returns OK if it responds anything.
 *
 */
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
      status = HttpUtils.httpGET(HTTPS_OSLOBYSYKKEL_NO_API_V_1_STATUS, apiKey);
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

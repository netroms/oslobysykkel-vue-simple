package no.inarctica.oslobysykkel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class HttpUtils {

  /**
   * Simple http client for doing GET requests using Java 11 HttpClient
   * @param uri fullUri to call GET
   * @param apiKey oslobysykkel api key to use in Client-Identifier header
   * @return raw response
   * @throws Exception
   */
  public static String httpGET(String uri, String apiKey) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .header("Client-Identifier", apiKey)
        .uri(URI.create(uri))
        .build();
    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
    return response.body();
  }
}

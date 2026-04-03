package se.lnu.clients;

import se.lnu.runner.TestConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * REST client — atomic HTTP executor only.
 * No traversal logic, no metrics. TestRunner owns the loop.
 */
public class RestClient {

    private final HttpClient http = HttpClient.newHttpClient();

    public HttpResponse<String> fetchRoot() throws IOException, InterruptedException {
        return get(TestConfig.BASE_URL + "/api/rest/root");
    }

    public HttpResponse<String> fetchChildren(String nodeId) throws IOException, InterruptedException {
        return get(TestConfig.BASE_URL + "/api/rest/node/" + nodeId + "/children");
    }

    private HttpResponse<String> get(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }
}

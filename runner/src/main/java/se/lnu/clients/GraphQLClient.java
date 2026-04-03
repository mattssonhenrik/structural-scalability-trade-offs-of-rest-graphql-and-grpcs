package se.lnu.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.lnu.runner.TestConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * GraphQL client — atomic HTTP executor only.
 * Builds the recursive field-selection query and fires one POST.
 * No metrics, no RunResult. TestRunner owns measurement and assembly.
 */
public class GraphQLClient {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public HttpResponse<String> fetch(int d, int k) throws Exception {
        String query = buildQuery(d, k);
        String body = mapper.writeValueAsString(Map.of("query", query));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(TestConfig.BASE_URL + "/graphql"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── private ───────────────────────────────────────────────────────────────

    /**
     * Builds a recursive GraphQL query for depth D requesting fields k00..k(K-1).
     *
     * Example D=2, K=2:
     * { root { id k00 k01 children { id k00 k01 children { id k00 k01 } } } }
     */
    private String buildQuery(int d, int k) {
        StringBuilder sb = new StringBuilder("{ root { id ");
        appendFields(sb, k);
        appendChildren(sb, d, k, 0);
        sb.append("} }");
        return sb.toString();
    }

    private void appendFields(StringBuilder sb, int k) {
        for (int i = 0; i < k; i++) {
            sb.append(String.format("k%02d ", i));
        }
    }

    private void appendChildren(StringBuilder sb, int targetDepth, int k, int currentDepth) {
        if (currentDepth >= targetDepth) return;
        sb.append("children { id ");
        appendFields(sb, k);
        appendChildren(sb, targetDepth, k, currentDepth + 1);
        sb.append("} ");
    }
}

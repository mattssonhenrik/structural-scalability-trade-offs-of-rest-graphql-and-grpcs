package se.lnu.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.lnu.model.RunResult;
import se.lnu.model.TestCase;
import se.lnu.runner.MetricsAccumulator;
import se.lnu.runner.TestConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * GraphQL client — fetches the full tree in one request.
 *
 * Builds a recursive query string matching the target depth D and field count
 * K.
 * GraphQL returns only the queried fields.
 * DP1 is always 1. DP2 and DP3 are read from the single response.
 */
public class GraphQLClient {

    private HttpClient http = HttpClient.newHttpClient();
    private ObjectMapper mapper = new ObjectMapper();

    /** Executes one GraphQL experiment run and returns measured metrics. */
    public RunResult run(TestCase testcase) {
        int dp1 = 0;
        int dp2 = 0;
        int dp3 = 0;

        try {
            String query = buildQuery(testcase.getD(), testcase.getK());
            String body = mapper.writeValueAsString(Map.of("query", query));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(TestConfig.BASE_URL + "/graphql"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            dp1++;
            dp3 += MetricsAccumulator.contentLength(resp);
            dp2 += MetricsAccumulator.orchestrationCount(resp);

            if (resp.statusCode() != 200) {
                return error(testcase, dp1, dp2, dp3);
            }

            int overfetch = MetricsAccumulator.overfetchGraphQL(resp.body(), testcase.getK());
            if (overfetch != 0) {
                return new RunResult(testcase, dp1, dp2, dp3, overfetch, 0, "error");
            }

            return new RunResult(testcase, dp1, dp2, dp3, 0, 0, "ok");

        } catch (Exception e) {
            return error(testcase, dp1, dp2, dp3);
        }
    }

    // ── private ───────────────────────────────────────────────────────────────

    /**
     * Builds a recursive GraphQL query for depth D requesting fields k00..k01...
     *
     * Example D=2, K=2:
     * { root { k00 k01 children { k00 k01 children { k00 k01 } } } }
     */
    private String buildQuery(int d, int k) {
        StringBuilder sb = new StringBuilder("{ root { id ");
        appendFields(sb, k);
        appendChildren(sb, d, k, 0);
        sb.append("} }");
        return sb.toString();
    }

    /** Appends k00..k(K-1) field names to the query builder. */
    private void appendFields(StringBuilder sb, int k) {
        for (int i = 0; i < k; i++) {
            sb.append(String.format("k%02d ", i));
        }
    }

    /** Recursively appends children selections up to targetDepth. */
    private void appendChildren(StringBuilder sb, int targetDepth, int k, int currentDepth) {
        if (currentDepth >= targetDepth)
            return;
        sb.append("children { id ");
        appendFields(sb, k);
        appendChildren(sb, targetDepth, k, currentDepth + 1);
        sb.append("} ");
    }

    /**
     * Returns a RunResult with status=error and whatever metrics were collected so
     * far.
     */
    private RunResult error(TestCase tc, int dp1, int dp2, int dp3) {
        return new RunResult(tc, dp1, dp2, dp3, 0, 0, "error");
    }
}

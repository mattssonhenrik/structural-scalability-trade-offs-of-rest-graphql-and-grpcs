package se.lnu.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.lnu.model.RunnerNode;
import se.lnu.model.RunResult;
import se.lnu.model.TestCase;
import se.lnu.runner.MetricsAccumulator;
import se.lnu.runner.TestConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * REST client, traverses the tree one level at a time via repeated HTTP calls.
 *
 * Implements the underfetch loop: fetches root, then iterates through a queue
 * fetching children for each non-leaf node until the target depth is reached.
 * Each GET counts as +1 DP1 and contributes its Content-Length to DP3.
 * DP2 is read from the X-Orchestration-Count response header per request.
 */
public class RestClient {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Executes one REST experiment run via the underfetch loop and returns measured
     * metrics.
     */
    public RunResult run(TestCase testCase) {
        int dp1 = 0;
        int dp2 = 0;
        int dp3 = 0;

        // queue entries: [nodeId, currentDepth]
        Deque<String[]> queue = new ArrayDeque<>();

        try {
            // fetch root
            HttpResponse<String> rootResp = get(TestConfig.BASE_URL + "/api/rest/root");
            dp1++;
            dp3 += MetricsAccumulator.contentLength(rootResp);
            dp2 += MetricsAccumulator.orchestrationCount(rootResp);

            if (rootResp.statusCode() != 200) {
                return error(testCase, dp1, dp2, dp3);
            }

            RunnerNode rootNode = mapper.readValue(rootResp.body(), RunnerNode.class);
            if (testCase.getD() > 0) {
                queue.add(new String[] { rootNode.getId(), "0" });
            }

            // underfetch loop — traverse until target depth
            while (!queue.isEmpty()) {
                String[] entry = queue.poll();
                String nodeId = entry[0];
                int nodeDepth = Integer.parseInt(entry[1]);

                HttpResponse<String> childResp = get(
                        TestConfig.BASE_URL + "/api/rest/node/" + nodeId + "/children");
                dp1++;
                dp3 += MetricsAccumulator.contentLength(childResp);
                dp2 += MetricsAccumulator.orchestrationCount(childResp);

                if (childResp.statusCode() != 200) {
                    return error(testCase, dp1, dp2, dp3);
                }

                if (nodeDepth + 1 < testCase.getD()) {
                    RunnerNode[] children = mapper.readValue(childResp.body(), RunnerNode[].class);
                    for (RunnerNode child : children) {
                        queue.add(new String[] { child.getId(), String.valueOf(nodeDepth + 1) });
                    }
                }
            }

            int overfetch = MetricsAccumulator.overfetchRest(TestConfig.K_MAX, testCase.getK(), testCase.getD(), testCase.getF());
            int underfetch = MetricsAccumulator.underfetchRest(dp1);

            return new RunResult(testCase, dp1, dp2, dp3, overfetch, underfetch, "ok");

        } catch (Exception e) {
            return error(testCase, dp1, dp2, dp3);
        }
    }

    // ── private ───────────────────────────────────────────────────────────────

    /** Sends a GET request and returns the response. */
    private HttpResponse<String> get(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Returns a RunResult with status=error and whatever metrics were collected so
     * far.
     */
    private RunResult error(TestCase tc, int dp1, int dp2, int dp3) {
        return new RunResult(tc, dp1, dp2, dp3, 0, 0, "error");
    }
}

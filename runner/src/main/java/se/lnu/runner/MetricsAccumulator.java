package se.lnu.runner;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for all DP1–DP5 measurement logic.
 *
 * Shared methods are paradigm-agnostic.
 * Paradigm-specific methods are grouped by API type.
 */
public class MetricsAccumulator {

    private static final ObjectMapper mapper = new ObjectMapper();

    // ── Shared ────────────────────────────────────────────────────────────────

    /** DP3: Content-Length header, falls back to body byte count. */
    public static int contentLength(HttpResponse<String> resp) {
        return resp.body().length();
    }

    /** DP2: X-Orchestration-Count header, error if absent. */
    public static int orchestrationCount(HttpResponse<String> resp) {
        return resp.headers().firstValue("X-Orchestration-Count")
                .map(Integer::parseInt)
                .orElseThrow(() -> new IllegalStateException(
                    "X-Orchestration-Count header missing — backend instrumentation did not run"));
    }

    

    // ── REST ──────────────────────────────────────────────────────────────────

    /** DP5 for REST: REST always returns all kMax fields, overfetch = surplus × nodes. */
    public static int overfetchRest(int kMax, int k, int d, int f) {
        return (kMax - k) * totalNodes(d, f);
    }

    /** DP4 for REST: every request beyond the first root call is an extra call. */
    public static int underfetchRest(int dp1) {
        return dp1 - 1;
    }

    // ── GraphQL ───────────────────────────────────────────────────────────────

    /**
     * DP5 for GraphQL: parses the response body and counts actual k-fields per node.
     * Returns total surplus fields across all nodes.
     * Should be 0 if the query was built correctly — non-zero indicates a bug.
     */
    @SuppressWarnings("unchecked")
    public static int overfetchGraphQL(String body, int expectedK) throws Exception {
        Map<String, Object> root = (Map<String, Object>)
                ((Map<String, Object>) mapper.readValue(body, Map.class).get("data")).get("root");
        return countOverfetchNode(root, expectedK);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static int countOverfetchNode(Map<String, Object> node, int expectedK) {
        int kCount = 0;
        for (String key : node.keySet()) {
            if (key.matches("k\\d+")) kCount++;
        }
        int overfetch = Math.max(0, kCount - expectedK);
        Object children = node.get("children");
        if (children instanceof List) {
            for (Map<String, Object> child : (List<Map<String, Object>>) children) {
                overfetch += countOverfetchNode(child, expectedK);
            }
        }
        return overfetch;
    }

    /** Total node count for a complete F-ary tree of depth D. */
    private static int totalNodes(int d, int f) {
        if (f == 1) return d + 1;
        return (int) ((Math.pow(f, d + 1) - 1) / (f - 1));
    }
}

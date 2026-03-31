package se.lnu.runner;

import se.lnu.clients.GraphQLClient;
import se.lnu.clients.RestClient;
import se.lnu.model.RunResult;
import se.lnu.model.TestCase;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Main experiment loop — runs the full OVAT sweep for REST and GraphQL.
 *
 * Three series: D-sweep (F=F_BASELINE, K=K_BASELINE),
 * F-sweep (D=D_BASELINE, K=K_BASELINE),
 * K-sweep (D=D_BASELINE, F=F_BASELINE).
 *
 * Before each test case the DataStore is reloaded via POST /api/admin/reload.
 * Results are written to CSV files in runner/results/.
 */
public class TestRunner {

    private RestClient restClient = new RestClient();
    private GraphQLClient graphQLClient = new GraphQLClient();
    private HttpClient http = HttpClient.newHttpClient();

    /** Runs all three OVAT series (D, F, K) and writes results to CSV. */
    public void run() throws IOException {
        runSeries("D", TestConfig.D_BASELINE, TestConfig.F_BASELINE, TestConfig.K_BASELINE);
        runSeries("F", TestConfig.D_BASELINE, TestConfig.F_BASELINE, TestConfig.K_BASELINE);
        runSeries("K", TestConfig.D_BASELINE, TestConfig.F_BASELINE, TestConfig.K_BASELINE);
    }

    // ── private ───────────────────────────────────────────────────────────────

    /**
     * Sweeps one variable (D, F, or K) from SWEEP_MIN to SWEEP_MAX, writes one CSV
     * file.
     */
    private void runSeries(String series, int dBase, int fBase, int kBase) throws IOException {
        CsvWriter csv = new CsvWriter("results/rq1_" + series + "_series_" + buildTag(series) + ".csv", series);

        // loopar series variabel från min till max
        for (int value = TestConfig.SWEEP_MIN; value <= TestConfig.SWEEP_MAX; value++) {
            int d = series.equals("D") ? value : dBase;
            int f = series.equals("F") ? value : fBase;
            int k = series.equals("K") ? value : kBase;

            // F=0 is meaningless (no children), skip
            if (f == 0)
                continue;
            // K=0 means no fields, skip
            if (k == 0)
                continue;

            // Dataset always generated with K_MAX fields — target k varies per test case
            reload(d, f, TestConfig.K_MAX);

            for (String paradigm : TestConfig.PARADIGMS) {
                for (int run = 1; run <= TestConfig.N_RUNS; run++) {
                    TestCase testcase = new TestCase(series, paradigm, d, f, k, run);
                    RunResult result = runSingle(paradigm, testcase);
                    csv.appendRow(result);
                    System.out.printf("%-8s %-7s D=%2d F=%2d K=%2d run=%d → %s%n",
                            paradigm, series, d, f, k, run, result.getStatus());
                }
            }
        }

        csv.close();
    }

    /** Dispatches one run to the correct client based on paradigm. */
    private RunResult runSingle(String paradigm, TestCase testcase) {
        if (paradigm.equals("REST"))
            return restClient.run(testcase);
        if (paradigm.equals("GraphQL"))
            return graphQLClient.run(testcase);
        throw new IllegalArgumentException("Unknown paradigm: " + paradigm);
    }

    /**
     * Builds a filename tag encoding the fixed and swept parameters, e.g.
     * "D2-5_F5_K8".
     */
    private String buildTag(String series) {
        String minMaxString = TestConfig.SWEEP_MIN + "-" + TestConfig.SWEEP_MAX;
        String d = series.equals("D") ? "D" + minMaxString : "D" + TestConfig.D_BASELINE;
        String f = series.equals("F") ? "F" + minMaxString : "F" + TestConfig.F_BASELINE;
        String k = series.equals("K") ? "K" + minMaxString : "K" + TestConfig.K_MAX;
        return d + "_" + f + "_" + k;
    }

    /**
     * Calls POST /api/admin/reload to regenerate the dataset before each test case.
     */
    private void reload(int d, int f, int k) {
        String url = String.format("%s/api/admin/reload?D=%d&F=%d&K=%d&seed=%d",
                TestConfig.BASE_URL, d, f, k, TestConfig.SEED);
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            http.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            throw new RuntimeException("Reload failed for D=" + d + " F=" + f + " K=" + k, e);
        }
    }
}

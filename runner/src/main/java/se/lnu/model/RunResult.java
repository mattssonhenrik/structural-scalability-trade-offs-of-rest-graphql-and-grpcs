package se.lnu.model;

/**
 * Stores the measured results for one experiment run.
 *
 * DP1 = client-to-server requests
 * DP2 = server-side orchestration operations
 * DP3 = total response body bytes
 * overfetchFields = fields returned beyond target K
 * underfetchExtraCalls = extra requests beyond the first
 * status = ok, capped, or error
 */
public class RunResult {

    private TestCase testCase;
    private int dp1;
    private int dp2;
    private int dp3;
    private int overfetchFields;
    private int underfetchExtraCalls;
    private String status;

    public RunResult(TestCase testCase, int dp1, int dp2, int dp3,
            int overfetchFields, int underfetchExtraCalls, String status) {
        this.testCase = testCase;
        this.dp1 = dp1;
        this.dp2 = dp2;
        this.dp3 = dp3;
        this.overfetchFields = overfetchFields;
        this.underfetchExtraCalls = underfetchExtraCalls;
        this.status = status;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public int getDp1() {
        return dp1;
    }

    public int getDp2() {
        return dp2;
    }

    public int getDp3() {
        return dp3;
    }

    public int getOverfetchFields() {
        return overfetchFields;
    }

    public int getUnderfetchExtraCalls() {
        return underfetchExtraCalls;
    }

    public String getStatus() {
        return status;
    }
}

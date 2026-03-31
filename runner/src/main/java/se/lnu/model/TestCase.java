package se.lnu.model;

/**
 * Stores the independent variables for one experiment run.
 *
 * series = swept variable (D, F, or K)
 * paradigm = API under test (REST, GraphQL or gRPC)
 * d, f, k = test-case values
 * run = repetition number
 */
public class TestCase {

    private String series;
    private String paradigm;
    private int d;
    private int f;
    private int k;
    private int run;

    public TestCase(String series, String paradigm, int d, int f, int k, int run) {
        this.series = series;
        this.paradigm = paradigm;
        this.d = d;
        this.f = f;
        this.k = k;
        this.run = run;
    }

    public String getSeries() {
        return series;
    }

    public String getParadigm() {
        return paradigm;
    }

    public int getD() {
        return d;
    }

    public int getF() {
        return f;
    }

    public int getK() {
        return k;
    }

    public int getRun() {
        return run;
    }
}

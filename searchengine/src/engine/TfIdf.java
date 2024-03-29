package engine;

public class TfIdf {
    // normalize tf by tfmax
    public static double normalizeTermWeighting(double termWeighting, int tfmax) {
        return termWeighting / tfmax;
    }

    // calculate tf-idf
    public static double calculateTermWeighting(int tf, int df, int N) {
        double idf = log2((double) N / df);
        return tf * idf;
    }

    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
}

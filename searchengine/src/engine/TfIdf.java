package engine;

public class TfIdf {
    public static double normalizeTermWeighting(double termWeighting, int tfmax) {
        return termWeighting / tfmax;
    }

    public static double calculateTermWeighting(int tf, int df, int N) {
        return tf * log2(N / df);
    }

    public static double log2(int x) {
        return Math.log(x) / Math.log(2);
    }
}

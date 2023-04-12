package utilities;

public class CosineSimilarity {

    // calculate the cosine similarity between two vectors
    public static double calculate(double[] vector1, double[] vector2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vector1.length && i < vector2.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            normA += Math.pow(vector1[i], 2);
            normB += Math.pow(vector2[i], 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}

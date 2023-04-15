package engine;

import java.util.HashMap;
import java.util.Map;

public class CosineSimilarity {

    // calculate the document similarities between the query and the documents vectors
    public static Map<Integer, Double> getDocumentSimilarities(double[] queryVector, Map<Integer, double[]> documentVectors) {
        Map<Integer, Double> documentSimilarities = new HashMap<Integer, Double>();
        
        for (Integer docId : documentVectors.keySet()) {
            double[] documentVector = documentVectors.get(docId);
            double cosineSimilarity = calculateCosineSimilarity(queryVector, documentVector);
            documentSimilarities.put(docId, cosineSimilarity);
        }

        return documentSimilarities;
    }

    // calculate the cosine similarity between two vectors
    private static double calculateCosineSimilarity(double[] vector1, double[] vector2) {
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

package engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CosineSimilarityTest {
    @Test
    public void testCalculateCosineSimilarity1() {
        double[] vector1 = { 2, 3, 5 };
        double[] vector2 = { 0, 0, 2 };

        double cosineSimilarity = CosineSimilarity.calculateCosineSimilarity(vector1, vector2);

        assertEquals(cosineSimilarity, 0.81, 0.01);
    }

    @Test
    public void testCalculateCosineSimilarity2() {
        double[] vector1 = { 3, 7, 1 };
        double[] vector2 = { 0, 0, 2 };

        double cosineSimilarity = CosineSimilarity.calculateCosineSimilarity(vector1, vector2);

        assertEquals(cosineSimilarity, 0.13, 0.01);
    }
}

package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import storage.Posting;
import utilities.Constants;
import utilities.CosineSimilarity;
import utilities.Result;
import utilities.Tokenizer;

class Engine {
    private Retriever retriever;
    private Tokenizer tokenizer;

    public Engine() throws IOException {
        tokenizer = new Tokenizer();
        retriever = new Retriever(Constants.STORAGE_NAME);
    }

    public void commitAndClose() throws IOException {
        retriever.commitAndClose();
    }

    public List<Result> search(String query) throws IOException {
        List<String> queryWords = tokenizer.tokenize(query);
        List<Integer> queryWordIds = retriever.getWordIds(queryWords);

        if (queryWordIds.isEmpty()) {
            return new ArrayList<>();
        }

        int vocabularySize = retriever.getNumberOfWords();
        int numberOfDocs = retriever.getNumberOfDocuments();

        double[] queryVector = calculateQueryVector(queryWordIds, vocabularySize, numberOfDocs);

        Set<Integer> relevantDocuments = new HashSet<Integer>();
        for (Integer wordId : queryWordIds) {
            Map<Integer, Posting> titlePostings = retriever.getTitlePostings(wordId);
            Map<Integer, Posting> bodyPostings = retriever.getBodyPostings(wordId);

            relevantDocuments.addAll(titlePostings.keySet());
            relevantDocuments.addAll(bodyPostings.keySet());
        }

        Map<Integer, double[]> documentVectors = new HashMap<Integer, double[]>();
        for (Integer docId : relevantDocuments) {
            double[] documentVector = calculateDocumentVector(docId, vocabularySize, numberOfDocs);
            documentVectors.put(docId, documentVector);
        }

        Map<Integer, Double> documentSimilarities = getDocumentSimilarities(queryVector, documentVectors);
        List<Result> rankedResults = retriever.getRankedResults(documentSimilarities);

        return rankedResults;
    }

    private Map<Integer, Double> getDocumentSimilarities(double[] queryVector, Map<Integer, double[]> documentVectors) {
        Map<Integer, Double> documentSimilarities = new HashMap<Integer, Double>();
        
        for (Integer docId : documentVectors.keySet()) {
            double[] documentVector = documentVectors.get(docId);
            double cosineSimilarity = CosineSimilarity.calculate(queryVector, documentVector);
            documentSimilarities.put(docId, cosineSimilarity);
        }

        return documentSimilarities;
    }

    private double[] calculateDocumentVector(Integer docId, int vocabularySize, int N) throws IOException {
        double[] documentVector = new double[vocabularySize];

        Set<Integer> titleWordIds = retriever.getTitleWordIds(docId);
        Set<Integer> bodyWordIds = retriever.getBodyWordIds(docId);
        Set<Integer> wordIds = new HashSet<Integer>();
        wordIds.addAll(titleWordIds);
        wordIds.addAll(bodyWordIds);

        int tfmax = 1;

        for (Integer wordId : wordIds) {
            Map<Integer, Posting> titlePostings = retriever.getTitlePostings(wordId);
            Map<Integer, Posting> bodyPostings = retriever.getBodyPostings(wordId);

            Posting titlePosting = titlePostings.get(docId);
            Posting bodyPosting = bodyPostings.get(docId);

            int tf_title = titlePosting != null ? titlePosting.getFrequency() : 0;
            int tf_body = bodyPosting != null ? bodyPosting.getFrequency() : 0;

            int df = getDocumentFrequency(titlePostings, bodyPostings);
            tfmax = Math.max(tfmax, tf_title + tf_body);

            // todo: fix title weight
            double tf_idf_title = calculateTermWeighting(tf_title, df, N) * Constants.TITLE_WEIGHT;
            double tf_idf_body = calculateTermWeighting(tf_body, df, N);

            documentVector[wordId] = tf_idf_title + tf_idf_body;
        }

        for (int i = 0; i < documentVector.length; i++) {
            documentVector[i] = normalizeTermWeighting(documentVector[i], tfmax);
        }

        return documentVector;
    }

    private double[] calculateQueryVector(List<Integer> queryWordsIds, int vocabularySize, int N) throws IOException {
        double[] queryVector = new double[vocabularySize];

        Map<Integer, Long> wordFrequencies = queryWordsIds.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        int tfmax = wordFrequencies.values().stream().max(Long::compare).get().intValue();

        for (Integer wordId : wordFrequencies.keySet()) {
            int tf = wordFrequencies.get(wordId).intValue();
            int df = getDocumentFrequency(retriever.getTitlePostings(wordId), retriever.getBodyPostings(wordId));

            double tf_idf = normalizeTermWeighting(calculateTermWeighting(tf, df, N), tfmax);
            queryVector[wordId] = tf_idf;
        }

        return queryVector;
    }

    private double normalizeTermWeighting(double termWeighting, int tfmax) {
        return termWeighting / tfmax;
    }

    private double calculateTermWeighting(int tf, int df, int N) {
        return tf * log2(N / df);
    }

    private static double log2(int x) {
        return Math.log(x) / Math.log(2);
    }

    private int getDocumentFrequency(Map<Integer, Posting> titlePostings, Map<Integer, Posting> bodyPostings) {
        Set<Integer> uniqueDocIds = new HashSet<Integer>(titlePostings.keySet());
        uniqueDocIds.addAll(bodyPostings.keySet());
        return uniqueDocIds.size();
    }

    public static void main(String[] args) throws Exception {
        Engine searchEngine = new Engine();
        List<Result> results = searchEngine.search("hkust academics");
        
        for (Result result : results) {
            System.out.println(result);
        }

        searchEngine.commitAndClose();
    }
}

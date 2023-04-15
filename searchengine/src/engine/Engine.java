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

        Set<Integer> relevantDocuments = getRelevantDocuments(queryWordIds);

        Map<Integer, double[]> documentVectors = getDocumentVectors(vocabularySize, numberOfDocs, relevantDocuments);

        Map<Integer, Double> documentSimilarities = CosineSimilarity.getDocumentSimilarities(queryVector,
                documentVectors);
        List<Result> rankedResults = retriever.getRankedResults(documentSimilarities);

        return rankedResults;
    }

    private Set<Integer> getRelevantDocuments(List<Integer> queryWordIds) throws IOException {
        Set<Integer> relevantDocuments = new HashSet<Integer>();
        for (Integer wordId : queryWordIds) {
            Map<Integer, Posting> titlePostings = retriever.getTitlePostings(wordId);
            Map<Integer, Posting> bodyPostings = retriever.getBodyPostings(wordId);

            relevantDocuments.addAll(titlePostings.keySet());
            relevantDocuments.addAll(bodyPostings.keySet());
        }
        return relevantDocuments;
    }

    private Map<Integer, double[]> getDocumentVectors(int vocabularySize, int numberOfDocs,
            Set<Integer> relevantDocuments)
            throws IOException {
        Map<Integer, double[]> documentVectors = new HashMap<Integer, double[]>();
        for (Integer docId : relevantDocuments) {
            double[] documentVector = calculateDocumentVector(docId, vocabularySize, numberOfDocs);
            documentVectors.put(docId, documentVector);
        }
        return documentVectors;
    }

    private double[] calculateDocumentVector(Integer docId, int vocabularySize, int N) throws IOException {
        double[] documentVector = new double[vocabularySize];

        Set<Integer> wordIds = retriever.getDocumentWordIds(docId);

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
            double tf_idf_title = TfIdf.calculateTermWeighting(tf_title, df, N) * Constants.TITLE_WEIGHT;
            double tf_idf_body = TfIdf.calculateTermWeighting(tf_body, df, N);

            documentVector[wordId] = tf_idf_title + tf_idf_body;
        }

        for (int i = 0; i < documentVector.length; i++) {
            documentVector[i] = TfIdf.normalizeTermWeighting(documentVector[i], tfmax);
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

            double tf_idf = TfIdf.normalizeTermWeighting(TfIdf.calculateTermWeighting(tf, df, N), tfmax);
            queryVector[wordId] = tf_idf;
        }

        return queryVector;
    }

    private int getDocumentFrequency(Map<Integer, Posting> titlePostings, Map<Integer, Posting> bodyPostings) {
        Set<Integer> uniqueDocIds = new HashSet<Integer>(titlePostings.keySet());
        uniqueDocIds.addAll(bodyPostings.keySet());
        return Math.max(uniqueDocIds.size(), 1);
    }

    public static void main(String[] args) throws Exception {
        Engine searchEngine = new Engine();
        List<Result> results = searchEngine.search("movie");

        for (int i = 0; i < 50 && i < results.size(); i++) {
            System.out.println(results.get(i));
        }

        System.out.println("Number of results: " + results.size());

        searchEngine.commitAndClose();
    }
}

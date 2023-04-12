package engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import storage.Posting;
import utilities.Constants;
import utilities.CosineSimilarity;
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

    public List<Integer> search(String query) throws IOException {
        List<String> queryWords = tokenizer.tokenize(query);
        List<Integer> queryWordIds = retriever.getWordIds(queryWords);
        int vocabularySize = retriever.getNumberOfWords();

        double[] queryVector = calculateQueryVector(queryWordIds, vocabularySize);

        Set<Integer> relevantDocuments = new HashSet<Integer>();
        for (Integer wordId : queryWordIds) {
            Set<Posting> titlePostings = retriever.getTitlePostings(wordId);
            Set<Posting> bodyPostings = retriever.getBodyPostings(wordId);

            relevantDocuments.addAll(titlePostings.stream().map(Posting::getDocId).collect(Collectors.toSet()));
            relevantDocuments.addAll(bodyPostings.stream().map(Posting::getDocId).collect(Collectors.toSet()));
        }

        Map<Integer, double[]> documentVectors = new HashMap<Integer, double[]>();
        for (Integer docId : relevantDocuments) {
            double[] documentVector = calculateDocumentVector(docId, vocabularySize);
            documentVectors.put(docId, documentVector);
        }

        List<Integer> rankedDocuments = rankDocuments(queryVector, documentVectors);

        return rankedDocuments;
    }

    private List<Integer> rankDocuments(double[] queryVector, Map<Integer, double[]> documentVectors) {
        Map<Integer, Double> documentSimilarities = new HashMap<Integer, Double>();
        for (Integer docId : documentVectors.keySet()) {
            double[] documentVector = documentVectors.get(docId);
            double cosineSimilarity = CosineSimilarity.calculate(queryVector, documentVector);
            documentSimilarities.put(docId, cosineSimilarity);
        }

        return documentSimilarities.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double[] calculateDocumentVector(Integer docId, int vocabularySize) throws IOException {
        double[] documentVector = new double[vocabularySize];
        
        Set<Integer> titleWordIds = retriever.getTitleWordIds(docId);
        Set<Integer> bodyWordIds = retriever.getBodyWordIds(docId);
        Set<Integer> wordIds = new HashSet<Integer>();
        wordIds.addAll(titleWordIds);
        wordIds.addAll(bodyWordIds);

        int N = Constants.NUMBER_OF_DOCUMENTS;
        int tfmax = 1;

        for (Integer wordId : wordIds) {
            Set<Posting> titlePostings = retriever.getTitlePostings(wordId);
            Set<Posting> bodyPostings = retriever.getBodyPostings(wordId);

            int tf_title = titlePostings.stream().filter(p -> p.getDocId().equals(docId)).map(Posting::getFrequency).findFirst().orElse(0);
            int tf_body = bodyPostings.stream().filter(p -> p.getDocId().equals(docId)).map(Posting::getFrequency).findFirst().orElse(0);

            tfmax = Math.max(tfmax, tf_title + tf_body);

            Set<Integer> uniqueDocIds = titlePostings.stream().map(Posting::getDocId).collect(Collectors.toSet());
            uniqueDocIds.addAll(bodyPostings.stream().map(Posting::getDocId).collect(Collectors.toSet()));
            int df = uniqueDocIds.size();

            double tf_idf_title = calculateTermWeighting(tf_title, df, N) * Constants.TITLE_WEIGHT;
            double tf_idf_body = calculateTermWeighting(tf_body, df, N);

            documentVector[wordId] = tf_idf_title + tf_idf_body;
        }

        for (int i = 0; i < documentVector.length; i++) {
            documentVector[i] = normalizeTermWeighting(documentVector[i], tfmax);
        }

        return documentVector;
    }

    private double[] calculateQueryVector(List<Integer> queryWordsIds, int vocabularySize) throws IOException {
        double[] queryVector = new double[vocabularySize];

        Map<Integer, Long> wordFrequencies = queryWordsIds.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        int N = Constants.NUMBER_OF_DOCUMENTS;
        int tfmax = wordFrequencies.values().stream().max(Long::compare).get().intValue();

        for (Integer wordId : wordFrequencies.keySet()) {
            int tf = wordFrequencies.get(wordId).intValue();
            // todo: merge body postings and title postings
            int df = retriever.getBodyPostings(wordId).size();

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

    public static void main(String[] args) throws Exception {
        Engine searchEngine = new Engine();
        List<Integer> results = searchEngine.search("hkust academics");
        System.out.println(results);
        searchEngine.commitAndClose();
    }
}

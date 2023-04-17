package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import storage.Posting;
import utilities.Constants;
import utilities.Result;
import utilities.Token;
import utilities.Tokenizer;

public class Engine {
    private Tokenizer tokenizer;
    private Retriever retriever;

    public Engine() throws IOException {
        tokenizer = new Tokenizer();
        retriever = new Retriever(Constants.STORAGE_NAME);
    }

    public void commitAndClose() throws IOException {
        retriever.commitAndClose();
    }

    public List<Result> search(String query) throws IOException {
        System.out.println("Searching for: " + query);

        List<Token> queryTokens = tokenizer.tokenizeQuery(query);
        List<SearchToken> searchTokens = retriever.getSearchTokens(queryTokens);

        if (searchTokens.isEmpty()) {
            return new ArrayList<>();
        }

        int vocabularySize = retriever.getNumberOfWords();
        int numberOfDocs = retriever.getNumberOfDocuments();

        List<Integer> queryWordIds = searchTokens.stream().flatMap(token -> token.getWordIds().stream())
                .collect(Collectors.toList());
        double[] queryVector = calculateQueryVector(queryWordIds, vocabularySize, numberOfDocs);

        Set<Integer> relevantDocuments = getRelevantDocuments(searchTokens);

        Map<Integer, double[]> documentVectors = getDocumentVectors(vocabularySize, numberOfDocs, relevantDocuments);

        Map<Integer, Double> documentSimilarities = CosineSimilarity.getDocumentSimilarities(queryVector,
                documentVectors);
        List<Result> rankedResults = retriever.getRankedResults(documentSimilarities);

        return rankedResults;
    }

    private Set<Integer> getRelevantDocuments(List<SearchToken> queryTokens)
            throws IOException {
        Set<Integer> relevantDocuments = new HashSet<Integer>();

        for (SearchToken token : queryTokens) {
            if (token.isPhrase()) {
                List<Integer> wordIds = token.getWordIds();
                relevantDocuments.addAll(getRelevantDocumentForPhraseInTitle(wordIds));
                relevantDocuments.addAll(getRelevantDocumentForPhraseInBody(wordIds));
            } else {
                Integer wordId = token.getWordIds().get(0);
                Map<Integer, Posting> titlePostings = retriever.getTitlePostings(wordId);
                Map<Integer, Posting> bodyPostings = retriever.getBodyPostings(wordId);

                relevantDocuments.addAll(titlePostings.keySet());
                relevantDocuments.addAll(bodyPostings.keySet());
            }
        }

        return relevantDocuments;
    }

    public Set<Integer> getRelevantDocumentForPhraseInTitle(List<Integer> wordIds) throws IOException {
        Map<Integer, Set<Integer>> documentPositionsMap = new HashMap<Integer, Set<Integer>>();

        Map<Integer, Posting> titlePostings = retriever.getTitlePostings(wordIds.get(0));
        documentPositionsMap = titlePostings.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getPositions()));

        for (int i = 1; i < wordIds.size(); i++) {
            Map<Integer, Posting> nextTitlePostings = retriever.getTitlePostings(wordIds.get(i));
            documentPositionsMap = filterDocuments(documentPositionsMap, nextTitlePostings);
        }

        return documentPositionsMap.keySet();
    }

    public Set<Integer> getRelevantDocumentForPhraseInBody(List<Integer> wordIds) throws IOException {
        Map<Integer, Set<Integer>> documentPositionsMap = new HashMap<Integer, Set<Integer>>();

        Map<Integer, Posting> bodyPostings = retriever.getBodyPostings(wordIds.get(0));
        documentPositionsMap = bodyPostings.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getPositions()));

        for (int i = 1; i < wordIds.size(); i++) {
            Map<Integer, Posting> nextBodyPostings = retriever.getBodyPostings(wordIds.get(i));
            documentPositionsMap = filterDocuments(documentPositionsMap, nextBodyPostings);
        }

        return documentPositionsMap.keySet();
    }

    public static Map<Integer, Set<Integer>> filterDocuments(Map<Integer, Set<Integer>> documentPositionsMap,
            Map<Integer, Posting> nextPostings) {
        Map<Integer, Set<Integer>> filteredDocumentPositionsMap = new HashMap<Integer, Set<Integer>>();

        for (Map.Entry<Integer, Set<Integer>> entry : documentPositionsMap.entrySet()) {
            Integer docId = entry.getKey();
            Set<Integer> positions = entry.getValue();

            if (nextPostings.containsKey(docId)) {
                Set<Integer> nextPositions = nextPostings.get(docId).getPositions();
                Set<Integer> filteredPositions = new HashSet<Integer>();

                for (Integer position : positions) {
                    if (nextPositions.contains(position + 1)) {
                        filteredPositions.add(position + 1);
                    }
                }

                if (!filteredPositions.isEmpty()) {
                    filteredDocumentPositionsMap.put(docId, filteredPositions);
                }
            }
        }

        return filteredDocumentPositionsMap;
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

    public double[] calculateQueryVector(List<Integer> queryWordsIds, int vocabularySize, int N) throws IOException {
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

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your query: ");
        String query = scanner.nextLine();

        long startTime = System.currentTimeMillis();
        List<Result> results = searchEngine.search(query);
        long endTime = System.currentTimeMillis();

        for (int i = 0; i < 10 && i < results.size(); i++) {
            System.out.println(results.get(i));
        }

        System.out.println("Query: " + query);
        System.out.println("Number of results: " + results.size());
        System.out.println("Search Runtime: " + (endTime - startTime) + "ms");

        scanner.close();
        searchEngine.commitAndClose();
    }
}

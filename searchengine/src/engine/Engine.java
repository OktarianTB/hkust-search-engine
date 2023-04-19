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

// this class is responsible for the search engine logic
public class Engine {
    private Tokenizer tokenizer;
    private Retriever retriever;

    public Engine() throws IOException {
        tokenizer = new Tokenizer();
        retriever = new Retriever(Constants.STORAGE_NAME);
    }

    // close the database
    public void commitAndClose() throws IOException {
        retriever.commitAndClose();
    }

    // tokenize the query and search for the given query
    public List<Result> query(String query) throws IOException {
        System.out.println("Searching for: " + query);

        List<Token> queryTokens = tokenizer.tokenizeQuery(query);
        List<SearchToken> searchTokens = retriever.getSearchTokens(queryTokens);

        // no query words match words in the database
        if (searchTokens.isEmpty()) {
            return new ArrayList<>();
        }

        return search(searchTokens, Constants.NUMBER_OF_QUERY_RESULTS);
    }

    // get similar documents to the given document
    // this is done by getting the words in the document and uses them as the query
    // in a normal search
    public List<Result> getSimilarDocuments(Integer docId) throws IOException {
        System.out.println("Getting similar documents for doc: " + docId);

        Set<Integer> docWordIds = retriever.getDocumentWordIds(docId);

        // no such document in the database
        if (docWordIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<SearchToken> searchTokens = docWordIds.stream().map(wordId -> new SearchToken(List.of(wordId)))
                .collect(Collectors.toList());

        return search(searchTokens, Constants.NUMBER_OF_RELEVANT_DOCS_RESULTS);
    }

    // search for the given query
    public List<Result> search(List<SearchToken> searchTokens, int numberOfResults) throws IOException {
        int vocabularySize = retriever.getNumberOfWords();
        int numberOfDocs = retriever.getNumberOfDocuments();

        // calculate the query vector
        List<Integer> queryWordIds = searchTokens.stream().flatMap(token -> token.getWordIds().stream())
                .collect(Collectors.toList());
        double[] queryVector = calculateQueryVector(queryWordIds, vocabularySize, numberOfDocs);

        // find relevant documents
        Set<Integer> relevantDocuments = getRelevantDocuments(searchTokens);

        // calculate the document vectors
        Set<Integer> queryWordIdSet = new HashSet<Integer>(queryWordIds);
        Map<Integer, double[]> documentVectors = getDocumentVectors(relevantDocuments, queryWordIdSet, vocabularySize,
                numberOfDocs);

        // calculate the document similarities between each document vector and the
        // query vector
        Map<Integer, Double> documentSimilarities = CosineSimilarity.getDocumentSimilarities(queryVector,
                documentVectors);

        // get the top results
        List<Result> rankedResults = retriever.getRankedResults(documentSimilarities, numberOfResults);

        return rankedResults;
    }

    // find relevant documents
    // a document is relevant if it contains at least one of the query words
    private Set<Integer> getRelevantDocuments(List<SearchToken> queryTokens)
            throws IOException {
        Set<Integer> relevantDocuments = new HashSet<Integer>();

        for (SearchToken token : queryTokens) {
            if (token.isPhrase()) {
                // if the token is a phrase, only get documents that contain the phrase
                List<Integer> wordIds = token.getWordIds();
                relevantDocuments.addAll(getRelevantDocumentForPhraseInTitle(wordIds));
                relevantDocuments.addAll(getRelevantDocumentForPhraseInBody(wordIds));
            } else {
                // if the token is a single word, get documents that contain the word
                Integer wordId = token.getWordIds().get(0);
                Map<Integer, Posting> titlePostings = retriever.getTitlePostings(wordId);
                Map<Integer, Posting> bodyPostings = retriever.getBodyPostings(wordId);

                relevantDocuments.addAll(titlePostings.keySet());
                relevantDocuments.addAll(bodyPostings.keySet());
            }
        }

        return relevantDocuments;
    }

    // find all documents that contain the phrase in the title
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

    // find all documents that contain the phrase in the body
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

    // filter for documents that contain the phrase
    // this is done by checking if the next word in the phrase is in the same
    // document at the next position
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

    // calculate the document vectors for the relevant documents
    private Map<Integer, double[]> getDocumentVectors(Set<Integer> relevantDocuments, Set<Integer> queryWordIds,
            int vocabularySize,
            int numberOfDocs)
            throws IOException {
        Map<Integer, double[]> documentVectors = new HashMap<Integer, double[]>();
        for (Integer docId : relevantDocuments) {
            double[] documentVector = calculateDocumentVector(docId, queryWordIds, vocabularySize, numberOfDocs);
            documentVectors.put(docId, documentVector);
        }
        return documentVectors;
    }

    // calculate the document vector for a document
    private double[] calculateDocumentVector(Integer docId, Set<Integer> queryWordIds, int vocabularySize, int N)
            throws IOException {
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

            double tf_idf_title = TfIdf.calculateTermWeighting(tf_title, df, N);
            double tf_idf_body = TfIdf.calculateTermWeighting(tf_body, df, N);

            // if the word is in the query, increase the importance of the word in the title
            // this is done by multiplying the tf-idf value with a weight
            // if the title does not contain the word, the tf_idf will remain zero
            if (queryWordIds.contains(wordId)) {
                tf_idf_title *= Constants.TITLE_WEIGHT;
            }

            documentVector[wordId] = tf_idf_title + tf_idf_body;
        }

        // normalize the document vector by dividing each value by the maximum tf value
        // in the document
        for (int i = 0; i < documentVector.length; i++) {
            documentVector[i] = TfIdf.normalizeTermWeighting(documentVector[i], tfmax);
        }

        return documentVector;
    }

    // calculate the query vector
    public double[] calculateQueryVector(List<Integer> queryWordsIds, int vocabularySize, int N) throws IOException {
        double[] queryVector = new double[vocabularySize];

        // count the frequency of each word in the query
        Map<Integer, Long> wordFrequencies = queryWordsIds.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        int tfmax = wordFrequencies.values().stream().max(Long::compare).get().intValue();

        // calculate the tf-idf value for each word in the query
        for (Integer wordId : wordFrequencies.keySet()) {
            int tf = wordFrequencies.get(wordId).intValue();
            int df = getDocumentFrequency(retriever.getTitlePostings(wordId), retriever.getBodyPostings(wordId));

            double tf_idf = TfIdf.normalizeTermWeighting(TfIdf.calculateTermWeighting(tf, df, N), tfmax);
            queryVector[wordId] = tf_idf;
        }

        return queryVector;
    }

    // calculate the document frequency for a word by counting the number of
    // documents that contain the word
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
        List<Result> results = searchEngine.query(query);
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

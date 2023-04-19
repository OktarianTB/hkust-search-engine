package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jdbm.helper.FastIterator;
import storage.Posting;
import storage.Properties;
import storage.Relationship;
import storage.Storage;
import utilities.Result;
import utilities.Token;

// this class is responsible for retrieving documents from storage (read-only)
class Retriever extends Storage {
    // caches for the inverted indices
    private Map<Integer, Map<Integer, Posting>> titleCache;
    private Map<Integer, Map<Integer, Posting>> bodyCache;

    public Retriever(String recordManagerName) throws IOException {
        super(recordManagerName);

        titleCache = new HashMap<Integer, Map<Integer, Posting>>();
        bodyCache = new HashMap<Integer, Map<Integer, Posting>>();
    }

    // returns a list of search tokens from a list of tokens
    // a search token is a list of word ids (one for a single word token, multiple
    // for a phrase token)
    public List<SearchToken> getSearchTokens(List<Token> tokens) throws IOException {
        List<SearchToken> searchTokens = new ArrayList<SearchToken>();

        for (Token token : tokens) {
            List<Integer> wordIds = new ArrayList<Integer>();

            for (String word : token.getWords()) {
                Integer wordId = wordMap.get(word);
                if (wordId != null) {
                    wordIds.add(wordId);
                }
            }

            if (!wordIds.isEmpty()) {
                searchTokens.add(new SearchToken(wordIds));
            }
        }

        return searchTokens;
    }

    // returns the document postings for a word in the title
    public Map<Integer, Posting> getTitlePostings(Integer wordId) throws IOException {
        if (titleCache.containsKey(wordId)) {
            return titleCache.get(wordId);
        }

        Map<Integer, Posting> postings = titleInvertedIndexMap.get(wordId);
        if (postings == null) {
            titleCache.put(wordId, new HashMap<>());
            return new HashMap<>();
        }

        titleCache.put(wordId, postings);
        return postings;
    }

    // returns the document postings for a word in the body
    public Map<Integer, Posting> getBodyPostings(Integer wordId) throws IOException {
        if (bodyCache.containsKey(wordId)) {
            return bodyCache.get(wordId);
        }

        Map<Integer, Posting> postings = bodyInvertedIndexMap.get(wordId);
        if (postings == null) {
            bodyCache.put(wordId, new HashMap<>());
            return new HashMap<>();
        }

        bodyCache.put(wordId, postings);
        return postings;
    }

    // returns all word ids contained in a document
    public Set<Integer> getDocumentWordIds(Integer docId) throws IOException {
        Set<Integer> wordIds = new HashSet<Integer>();
        wordIds.addAll(getTitleWordIds(docId));
        wordIds.addAll(getBodyWordIds(docId));
        return wordIds;
    }

    // returns the word ids contained in the title of a document
    public Set<Integer> getTitleWordIds(Integer docId) throws IOException {
        Set<Integer> wordIds = titleForwardIndexMap.get(docId);
        if (wordIds == null) {
            return new HashSet<>();
        }
        return wordIds;
    }

    // returns the word ids contained in the body of a document
    public Set<Integer> getBodyWordIds(Integer docId) throws IOException {
        Set<Integer> wordIds = bodyForwardIndexMap.get(docId);
        if (wordIds == null) {
            return new HashSet<>();
        }
        return wordIds;
    }

    // returns the number of words in the vocabulary
    public int getNumberOfWords() throws IOException {
        FastIterator iterator = reverseWordMap.keys();
        Integer docId = (Integer) iterator.next();
        int count = 0;
        while (docId != null) {
            count++;
            docId = (Integer) iterator.next();
        }
        return count;
    }

    // returns the number of documents in the corpus
    public int getNumberOfDocuments() throws IOException {
        FastIterator iterator = propertiesMap.keys();
        Integer docId = (Integer) iterator.next();
        int count = 0;
        while (docId != null) {
            count++;
            docId = (Integer) iterator.next();
        }
        return count;
    }

    // ranks the results based on the document similarities and returns the top
    // results
    public List<Result> getRankedResults(Map<Integer, Double> documentSimilarities, int numberOfResults)
            throws IOException {
        // sort the document ids by their similarity scores
        List<Integer> rankedDocumentIds = documentSimilarities.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(numberOfResults)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Result> results = new ArrayList<Result>();

        // for every top document id, construct a result object
        for (Integer docId : rankedDocumentIds) {
            try {
                String url = reverseDocumentMap.get(docId);
                Properties properties = propertiesMap.get(docId);

                Relationship relationship = adjacencyMap.get(docId);
                // for every child doc id, get the url from the reverse document map and add it
                // to the list of child links
                List<String> childLinks = new ArrayList<String>();
                for (Integer childDocId : relationship.getChildDocIds()) {
                    String childLink = reverseDocumentMap.get(childDocId);
                    childLinks.add(childLink);
                }

                // do the same for parent links
                List<String> parentLinks = new ArrayList<String>();
                for (Integer parentDocId : relationship.getParentDocIds()) {
                    String parentLink = reverseDocumentMap.get(parentDocId);
                    parentLinks.add(parentLink);
                }

                // combine the title and body word ids into one set of words and their
                // frequencies
                Map<String, Integer> wordFrequencyMap = new HashMap<String, Integer>();

                Set<Integer> titleWordIds = titleForwardIndexMap.get(docId);
                for (Integer wordId : titleWordIds) {
                    String word = reverseWordMap.get(wordId);

                    Map<Integer, Posting> titlePostings = titleCache.get(wordId);
                    if (titlePostings != null) {
                        Posting titlePosting = titlePostings.get(docId);
                        if (titlePosting != null) {
                            int currentFrequency = wordFrequencyMap.getOrDefault(word, 0);
                            wordFrequencyMap.put(word, titlePosting.getFrequency() + currentFrequency);
                        }
                    }
                }

                Set<Integer> bodyWordIds = bodyForwardIndexMap.get(docId);
                for (Integer wordId : bodyWordIds) {
                    String word = reverseWordMap.get(wordId);

                    Map<Integer, Posting> bodyPostings = bodyCache.get(wordId);
                    if (bodyPostings != null) {
                        Posting bodyPosting = bodyPostings.get(docId);
                        if (bodyPosting != null) {
                            int currentFrequency = wordFrequencyMap.getOrDefault(word, 0);
                            wordFrequencyMap.put(word, bodyPosting.getFrequency() + currentFrequency);
                        }
                    }
                }

                double score = documentSimilarities.get(docId);

                // add result to output list
                Result result = new Result(docId, score, url, properties, wordFrequencyMap, parentLinks, childLinks);
                results.add(result);
            } catch (IOException ignore) {
                System.out.println("Error getting results for doc id: " + docId);
            }
        }

        return results;
    }
}

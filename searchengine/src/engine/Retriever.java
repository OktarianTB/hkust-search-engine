package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jdbm.helper.FastIterator;
import storage.Posting;
import storage.Properties;
import storage.Relationship;
import storage.Storage;
import utilities.Result;

// this class is responsible for retrieving documents from storage (read-only)
class Retriever extends Storage {

    public Retriever(String recordManagerName) throws IOException {
        super(recordManagerName);
    }

    // returns the list of word ids for the given list of words
    public List<Integer> getWordIds(List<String> words) throws IOException {
        List<Integer> wordIds = new ArrayList<Integer>();

        for (String word : words) {
            Integer wordId = wordMap.get(word);
            if (wordId != null) {
                wordIds.add(wordId);
            }
        }

        return wordIds;
    }

    public Set<Posting> getTitlePostings(Integer wordId) throws IOException {
        Set<Posting> postings = titleInvertedIndexMap.get(wordId);
        if (postings == null) {
            return new HashSet<>();
        }
        return postings;
    }

    public Set<Posting> getBodyPostings(Integer wordId) throws IOException {
        Set<Posting> postings = bodyInvertedIndexMap.get(wordId);
        if (postings == null) {
            return new HashSet<>();
        }
        return postings;
    }

    public Set<Integer> getTitleWordIds(Integer docId) throws IOException {
        Set<Integer> wordIds = titleForwardIndexMap.get(docId);
        if (wordIds == null) {
            return new HashSet<>();
        }
        return wordIds;
    }

    public Set<Integer> getBodyWordIds(Integer docId) throws IOException {
        Set<Integer> wordIds = bodyForwardIndexMap.get(docId);
        if (wordIds == null) {
            return new HashSet<>();
        }
        return wordIds;
    }

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

    public List<Result> getRankedResults(Map<Integer, Double> documentSimilarities) throws IOException {
        List<Integer> rankedDocumentIds = documentSimilarities.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Result> results = new ArrayList<Result>();

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

                // combine the title and body word ids into one set of words and their
                // frequencies
                Map<String, Integer> wordFrequencyMap = new HashMap<String, Integer>();

                Set<Integer> titleWordIds = titleForwardIndexMap.get(docId);
                for (Integer wordId : titleWordIds) {
                    String word = reverseWordMap.get(wordId);

                    Set<Posting> titlePostings = titleInvertedIndexMap.get(wordId);
                    if (titlePostings != null) {
                        Optional<Posting> titlePosting = titlePostings.stream()
                                .filter(posting -> posting.getDocId().equals(docId)).findFirst();
                        if (titlePosting.isPresent()) {
                            int currentFrequency = wordFrequencyMap.getOrDefault(word, 0);
                            wordFrequencyMap.put(word, titlePosting.get().getFrequency() + currentFrequency);
                        }
                    }
                }

                Set<Integer> bodyWordIds = bodyForwardIndexMap.get(docId);
                for (Integer wordId : bodyWordIds) {
                    String word = reverseWordMap.get(wordId);

                    Set<Posting> bodyPostings = bodyInvertedIndexMap.get(wordId);
                    if (bodyPostings != null) {
                        Optional<Posting> bodyPosting = bodyPostings.stream()
                                .filter(posting -> posting.getDocId().equals(docId)).findFirst();
                        if (bodyPosting.isPresent()) {
                            int currentFrequency = wordFrequencyMap.getOrDefault(word, 0);
                            wordFrequencyMap.put(word, bodyPosting.get().getFrequency() + currentFrequency);
                        }
                    }
                }

                double score = documentSimilarities.get(docId);

                // add result to output list
                Result result = new Result(score, url, properties, wordFrequencyMap, childLinks);
                results.add(result);
            } catch (IOException ignore) {
                System.out.println("Error getting results for doc id: " + docId);
            }
        }

        return results;
    }
}

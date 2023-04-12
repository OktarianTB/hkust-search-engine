package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jdbm.helper.FastIterator;
import storage.Posting;
import storage.Storage;

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
        Set<Integer> wordIds =  bodyForwardIndexMap.get(docId);
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
}

package crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import storage.Posting;
import storage.Properties;
import storage.Relationship;
import storage.Storage;

// this class is responsible for indexing documents (read/write)
public class Indexer extends Storage {

    public Indexer(String recordManagerName) throws IOException {
        super(recordManagerName);
    }

    // get the doc id for the given url if it exists, else create a new doc id
    public Integer getDocId(String url) throws IOException {
        Integer docId = documentMap.get(url);
        if (docId == null) {
            docId = documentMap.getNextDocId();
            documentMap.put(url, docId);
            reverseDocumentMap.put(docId, url);
        }
        return docId;
    }

    // get the word id for the given url if it exists, else create a new word id
    private Integer getWordId(String word) throws IOException {
        Integer wordId = wordMap.get(word);
        if (wordId == null) {
            wordId = wordMap.getNextWordId();
            wordMap.put(word, wordId);
            reverseWordMap.put(wordId, word);
        }
        return wordId;
    }

    // returns the list of word ids for the given list of words
    private List<Integer> getWordIds(List<String> words) throws IOException {
        List<Integer> wordIds = new ArrayList<Integer>();

        for (String word : words) {
            wordIds.add(getWordId(word));
        }

        return wordIds;
    }

    // check if the document needs to be updated based on whether the new last
    // modified date is newer than the previously recorded last modified (if it
    // exists)
    public boolean docNeedsUpdating(Integer docId, Date newLastModifiedAt) throws IOException {
        Properties properties = propertiesMap.get(docId);
        if (properties != null) {
            return newLastModifiedAt.after(properties.getLastModifiedAt());
        }
        return true;
    }

    // update the document with the given doc id in the storage maps
    public void updateDocument(Integer docId, Page page, List<String> titleWords, List<String> bodyWords)
            throws IOException {
        // get word ids
        List<Integer> titleWordIds = getWordIds(titleWords);
        List<Integer> bodyWordIds = getWordIds(bodyWords);

        // get word positions
        Map<Integer, Set<Integer>> titleWordPositions = new HashMap<Integer, Set<Integer>>();
        Map<Integer, Set<Integer>> bodyWordPositions = new HashMap<Integer, Set<Integer>>();

        for (int i = 0; i < titleWordIds.size(); i++) {
            Integer wordId = titleWordIds.get(i);
            Set<Integer> positions = titleWordPositions.getOrDefault(wordId, new HashSet<Integer>());
            positions.add(i);
            titleWordPositions.put(wordId, positions);
        }

        for (int i = 0; i < bodyWordIds.size(); i++) {
            Integer wordId = bodyWordIds.get(i);
            Set<Integer> positions = bodyWordPositions.getOrDefault(wordId, new HashSet<Integer>());
            positions.add(i);
            bodyWordPositions.put(wordId, positions);
        }

        // get unique word ids
        Set<Integer> uniqueTitleWordIds = new HashSet<Integer>(titleWordIds);
        Set<Integer> uniqueBodyWordIds = new HashSet<Integer>(bodyWordIds);

        // update forward index map
        titleForwardIndexMap.put(docId, uniqueTitleWordIds);
        bodyForwardIndexMap.put(docId, uniqueBodyWordIds);

        // update title inverted index
        for (Integer wordId : uniqueTitleWordIds) {
            Set<Integer> wordPositions = titleWordPositions.get(wordId);
            Posting newPosting = new Posting(wordPositions.size(), wordPositions);

            Map<Integer, Posting> currentDocIdToPostingsMap = titleInvertedIndexMap.get(wordId);
            if (currentDocIdToPostingsMap != null) {
                // update the posting map for this word id
                Map<Integer, Posting> newDocIdToPostingsMap = new HashMap<Integer, Posting>(currentDocIdToPostingsMap);
                newDocIdToPostingsMap.put(docId, newPosting);
                titleInvertedIndexMap.put(wordId, newDocIdToPostingsMap);
            } else {
                titleInvertedIndexMap.put(wordId, Map.of(docId, newPosting));
            }
        }

        // update body inverted index
        for (Integer wordId : uniqueBodyWordIds) {
            Set<Integer> wordPositions = bodyWordPositions.get(wordId);
            Posting newPosting = new Posting(wordPositions.size(), wordPositions);

            Map<Integer, Posting> currentDocIdToPostingsMap = bodyInvertedIndexMap.get(wordId);
            if (currentDocIdToPostingsMap != null) {
                // update the posting map for this word id
                Map<Integer, Posting> newDocIdToPostingsMap = new HashMap<Integer, Posting>(currentDocIdToPostingsMap);
                newDocIdToPostingsMap.put(docId, newPosting);
                bodyInvertedIndexMap.put(wordId, newDocIdToPostingsMap);
            } else {
                bodyInvertedIndexMap.put(wordId, Map.of(docId, newPosting));
            }
        }

        // update properties map
        Properties properties = new Properties(page.getTitle(), page.getSize(), page.getLastModifiedAt());
        propertiesMap.put(docId, properties);
    }

    // update adjacency matrix map with new parent-child relationships
    public void updateRelationships(Integer parentDocId, Set<Integer> childDocIds)
            throws IOException {
        // update adjacency map of parent with new children
        Relationship parentRelationship = adjacencyMap.get(parentDocId);
        if (parentRelationship != null) {
            adjacencyMap.put(parentDocId, new Relationship(parentRelationship.getParentDocIds(), childDocIds));
        } else {
            adjacencyMap.put(parentDocId, new Relationship(Set.of(), childDocIds));
        }

        // update adjacency map of each child with new parent
        for (Integer childDocId : childDocIds) {
            Relationship childRelationship = adjacencyMap.get(childDocId);

            if (childRelationship != null) {
                Set<Integer> newParentDocIds = new HashSet<Integer>(childRelationship.getParentDocIds());
                newParentDocIds.add(parentDocId);

                adjacencyMap.put(childDocId, new Relationship(newParentDocIds, childRelationship.getChildDocIds()));
            } else {
                adjacencyMap.put(childDocId, new Relationship(Set.of(parentDocId), Set.of()));
            }
        }
    }
}

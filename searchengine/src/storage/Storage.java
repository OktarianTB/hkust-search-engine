package storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import utilities.Result;

// the storage class is responsible for managing storage, updating documents, and outputting results
public class Storage {
    private RecordManager recordManager;

    private WordMap wordMap;
    private ReverseWordMap reverseWordMap;
    private DocumentMap documentMap;
    private ReverseDocumentMap reverseDocumentMap;
    private TitleInvertedIndexMap titleInvertedIndexMap;
    private BodyInvertedIndexMap bodyInvertedIndexMap;
    private TitleForwardIndexMap titleForwardIndexMap;
    private BodyForwardIndexMap bodyForwardIndexMap;
    private AdjacencyMap adjacencyMap;
    private PropertiesMap propertiesMap;

    public Storage(String recordManagerName) throws IOException {
        recordManager = RecordManagerFactory.createRecordManager(recordManagerName);

        // initialize all storage maps
        wordMap = new WordMap(recordManager);
        reverseWordMap = new ReverseWordMap(recordManager);

        documentMap = new DocumentMap(recordManager);
        reverseDocumentMap = new ReverseDocumentMap(recordManager);

        titleInvertedIndexMap = new TitleInvertedIndexMap(recordManager);
        bodyInvertedIndexMap = new BodyInvertedIndexMap(recordManager);

        titleForwardIndexMap = new TitleForwardIndexMap(recordManager);
        bodyForwardIndexMap = new BodyForwardIndexMap(recordManager);

        adjacencyMap = new AdjacencyMap(recordManager);

        propertiesMap = new PropertiesMap(recordManager);
    }

    public void commitAndClose() throws IOException {
        recordManager.commit();
        recordManager.close();
    }

    // create a list of results containing all the information needed for the test
    // program
    public List<Result> getResults() throws IOException {
        List<Result> results = new ArrayList<Result>();

        FastIterator iterator = propertiesMap.keys();
        Integer docId = (Integer) iterator.next();
        while (docId != null) {
            final int innerDocId = docId;

            try {
                Properties properties = propertiesMap.get(docId);

                Relationship relationship = adjacencyMap.get(docId);
                // for every child doc id, get the url from the reverse document map and add it
                // to the list of child links
                List<String> childLinks = new ArrayList<String>();
                for (Integer childDocId : relationship.getChildDocIds()) {
                    childLinks.add(reverseDocumentMap.get(childDocId));
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
                                .filter(posting -> posting.getDocId().equals(innerDocId)).findFirst();
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
                                .filter(posting -> posting.getDocId().equals(innerDocId)).findFirst();
                        if (bodyPosting.isPresent()) {
                            int currentFrequency = wordFrequencyMap.getOrDefault(word, 0);
                            wordFrequencyMap.put(word, bodyPosting.get().getFrequency() + currentFrequency);
                        }
                    }
                }

                Result result = new Result(properties.getTitle(), properties.getUrl(), properties.getSize(),
                        properties.getLastModifiedAt(), wordFrequencyMap, childLinks);
                results.add(result);
            } catch (IOException ignore) {
            }

            docId = (Integer) iterator.next();
        }

        return results;
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
    public Integer getWordId(String word) throws IOException {
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
    public void updateDocument(Integer docId, Properties properties, List<String> titleWords, List<String> bodyWords)
            throws IOException {
        // update properties map
        propertiesMap.put(docId, properties);

        // get word ids and frequencies
        List<Integer> titleWordIds = getWordIds(titleWords);
        List<Integer> bodyWordIds = getWordIds(bodyWords);

        Map<Integer, Long> titleWordFrequencies = titleWordIds.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        Map<Integer, Long> bodyWordFrequencies = bodyWordIds.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        // get unique word ids
        Set<Integer> uniqueTitleWordIds = new HashSet<Integer>(titleWordIds);
        Set<Integer> uniqueBodyWordIds = new HashSet<Integer>(bodyWordIds);
                
        // update forward index map
        titleForwardIndexMap.put(docId, uniqueTitleWordIds);
        bodyForwardIndexMap.put(docId, uniqueBodyWordIds);

        // update title inverted index
        for (Integer wordId : uniqueTitleWordIds) {
            Posting newPosting = new Posting(docId, titleWordFrequencies.get(wordId).intValue());

            Set<Posting> currentPostings = titleInvertedIndexMap.get(wordId);
            if (currentPostings != null) {
                // remove the old posting for this doc id (if it exists) and add the new posting
                Set<Posting> newPostings = new HashSet<Posting>(currentPostings);
                newPostings.removeIf(posting -> posting.getDocId().equals(docId));
                newPostings.add(newPosting);
                titleInvertedIndexMap.put(wordId, newPostings);
            } else {
                titleInvertedIndexMap.put(wordId, Set.of(newPosting));
            }
        }

        // update body inverted index
        for (Integer wordId : uniqueBodyWordIds) {
            Posting newPosting = new Posting(docId, bodyWordFrequencies.get(wordId).intValue());

            Set<Posting> currentPostings = bodyInvertedIndexMap.get(wordId);
            if (currentPostings != null) {
                // remove the old posting for this doc id (if it exists) and add the new posting
                Set<Posting> newPostings = new HashSet<Posting>(currentPostings);
                newPostings.removeIf(posting -> posting.getDocId().equals(docId));
                newPostings.add(newPosting);
                bodyInvertedIndexMap.put(wordId, newPostings);
            } else {
                bodyInvertedIndexMap.put(wordId, Set.of(newPosting));
            }
        }
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

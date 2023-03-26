package storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import utilities.Pair;
import utilities.Result;

public class Storage {
    private RecordManager recordManager;

    private WordMap wordMap;
    private ReverseWordMap reverseWordMap;
    private DocumentMap documentMap;
    private ReverseDocumentMap reverseDocumentMap;
    private TitleInvertedIndexMap titleInvertedIndexMap;
    private BodyInvertedIndexMap bodyInvertedIndexMap;
    private ForwardIndexMap forwardIndexMap;
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
        forwardIndexMap = new ForwardIndexMap(recordManager);
        adjacencyMap = new AdjacencyMap(recordManager);
        propertiesMap = new PropertiesMap(recordManager);
    }

    public void commitAndClose() throws IOException {
        recordManager.commit();
        recordManager.close();
    }

    public List<Result> getResults() throws IOException {
        List<Result> results = new ArrayList<Result>();

        FastIterator iterator = propertiesMap.keys();
        Integer docId = (Integer) iterator.next();
        while (docId != null) {
            final int innerDocId = docId;

            Properties properties = propertiesMap.get(docId);

            Relationship relationship = adjacencyMap.get(docId);
            List<String> childLinks = new ArrayList<String>();
            for (Integer childDocId : relationship.getChildDocIds()) {
                childLinks.add(reverseDocumentMap.get(childDocId));
            }

            List<Pair> pairs = new ArrayList<Pair>();
            Set<Integer> wordIds = forwardIndexMap.get(docId);
            for (Integer wordId : wordIds) {
                String word = reverseWordMap.get(wordId);
                int frequency = 0;

                List<Posting> titlePostings = titleInvertedIndexMap.get(wordId);
                if (titlePostings != null) {
                    Optional<Posting> titlePosting = titlePostings.stream()
                            .filter(posting -> posting.getDocId().equals(innerDocId)).findFirst();
                    if (titlePosting.isPresent()) {
                        frequency += titlePosting.get().getFrequency();
                    }
                }

                List<Posting> bodyPostings = bodyInvertedIndexMap.get(wordId);
                if (bodyPostings != null) {
                    Optional<Posting> bodyPosting = bodyPostings.stream()
                            .filter(posting -> posting.getDocId().equals(innerDocId)).findFirst();
                    if (bodyPosting.isPresent()) {
                        frequency += bodyPosting.get().getFrequency();
                    }
                }

                if (frequency > 0) {
                    pairs.add(new Pair(word, frequency));
                }
            }

            Result result = new Result(properties.getTitle(), properties.getUrl(), properties.getSize(),
                    properties.getLastModifiedAt(), pairs, childLinks);
            results.add(result);

            docId = (Integer) iterator.next();
        }

        return results;
    }

    public Integer getDocId(String url) throws IOException {
        Integer docId = documentMap.get(url);
        if (docId == null) {
            docId = documentMap.getNextDocId();
            documentMap.put(url, docId);
            reverseDocumentMap.put(docId, url);
        }
        return docId;
    }

    public Integer getWordId(String word) throws IOException {
        Integer wordId = wordMap.get(word);
        if (wordId == null) {
            wordId = wordMap.getNextWordId();
            wordMap.put(word, wordId);
            reverseWordMap.put(wordId, word);
        }
        return wordId;
    }

    private List<Integer> getWordIds(List<String> words) throws IOException {
        List<Integer> wordIds = new ArrayList<Integer>();

        for (String word : words) {
            wordIds.add(getWordId(word));
        }

        return wordIds;
    }

    public boolean docNeedsUpdating(Integer docId, Date newLastModifiedAt) throws IOException {
        Properties properties = propertiesMap.get(docId);
        if (properties != null) {
            return newLastModifiedAt.after(properties.getLastModifiedAt());
        }
        return true;
    }

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

        // update forward index map
        Set<Integer> uniqueTitleWordIds = new HashSet<Integer>(titleWordIds);
        Set<Integer> uniqueBodyWordIds = new HashSet<Integer>(bodyWordIds);

        Set<Integer> uniqueWordIds = new HashSet<Integer>(uniqueTitleWordIds);
        uniqueWordIds.addAll(uniqueBodyWordIds);
        forwardIndexMap.put(docId, uniqueWordIds);

        // update title inverted index
        for (Integer wordId : uniqueTitleWordIds) {
            Posting newPosting = new Posting(docId, titleWordFrequencies.get(wordId).intValue());

            List<Posting> postings = titleInvertedIndexMap.get(wordId);
            if (postings != null) {
                List<Posting> newPostings = postings.stream().filter(posting -> !posting.getDocId().equals(docId))
                        .collect(Collectors.toList());
                newPostings.add(newPosting);

                titleInvertedIndexMap.put(wordId, newPostings);
            } else {
                titleInvertedIndexMap.put(wordId, List.of(newPosting));
            }
        }

        // update body inverted index
        for (Integer wordId : uniqueBodyWordIds) {
            Posting newPosting = new Posting(docId, bodyWordFrequencies.get(wordId).intValue());

            List<Posting> postings = bodyInvertedIndexMap.get(wordId);
            if (postings != null) {
                List<Posting> newPostings = postings.stream().filter(posting -> !posting.getDocId().equals(docId))
                        .collect(Collectors.toList());
                newPostings.add(newPosting);

                bodyInvertedIndexMap.put(wordId, newPostings);
            } else {
                bodyInvertedIndexMap.put(wordId, List.of(newPosting));
            }
        }
    }

    public void updateRelationships(Integer parentDocId, Set<Integer> childDocIds)
            throws IOException {
        // update adjacency map of parent
        Relationship parentRelationship = adjacencyMap.get(parentDocId);
        if (parentRelationship != null) {
            adjacencyMap.put(parentDocId, new Relationship(parentRelationship.getParentDocIds(), childDocIds));
        } else {
            adjacencyMap.put(parentDocId, new Relationship(Set.of(), childDocIds));
        }

        // update adjacency map of children
        for (Integer childDocId : childDocIds) {
            Relationship childRelationship = adjacencyMap.get(childDocId);
            if (childRelationship != null) {
                Set<Integer> newParentDocIds = childRelationship.getParentDocIds().stream()
                        .filter(docId -> !docId.equals(parentDocId)).collect(Collectors.toSet());
                newParentDocIds.add(parentDocId);

                adjacencyMap.put(childDocId, new Relationship(newParentDocIds, childRelationship.getChildDocIds()));
            } else {
                adjacencyMap.put(childDocId, new Relationship(Set.of(parentDocId), Set.of()));
            }
        }
    }
}

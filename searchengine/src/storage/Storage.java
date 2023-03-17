package storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

public class Storage {
    private RecordManager recordManager;

    private WordMap wordMap;
    private DocumentMap documentMap;
    private TitleInvertedIndexMap titleInvertedIndexMap;
    private BodyInvertedIndexMap bodyInvertedIndexMap;
    private ForwardIndexMap forwardIndexMap;
    private AdjacencyMap adjacencyMap;
    private PropertiesMap propertiesMap;

    public Storage(String recordManagerName) throws IOException {
        recordManager = RecordManagerFactory.createRecordManager(recordManagerName);

        // initialize all storage maps
        wordMap = new WordMap(recordManager);
        documentMap = new DocumentMap(recordManager);
        titleInvertedIndexMap = new TitleInvertedIndexMap(recordManager);
        bodyInvertedIndexMap = new BodyInvertedIndexMap(recordManager);
        forwardIndexMap = new ForwardIndexMap(recordManager);
        adjacencyMap = new AdjacencyMap(recordManager);
        propertiesMap = new PropertiesMap(recordManager);
    }

    public void commitAndClose() throws IOException {
        documentMap.print();
        propertiesMap.print();
        // forwardIndexMap.print();
        // wordMap.print();
        titleInvertedIndexMap.print();
        bodyInvertedIndexMap.print();

        recordManager.commit();
        recordManager.close();
    }

    public Integer getDocId(String url) throws IOException {
        Integer docId = documentMap.get(url);
        if (docId == null) {
            docId = documentMap.getNextDocId();
            documentMap.put(url, docId);
        }
        return docId;
    }

    public Integer getWordId(String word) throws IOException {
        Integer wordId = wordMap.get(word);
        if (wordId == null) {
            wordId = wordMap.getNextWordId();
            wordMap.put(word, wordId);
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
        forwardIndexMap.put(docId, uniqueBodyWordIds);

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
}

package storage;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

public class Storage {
    private RecordManager recordManager;

    private WordMap wordMap;
    private DocumentMap documentMap;
    private InvertedIndexMap invertedIndexMap;
    private ForwardIndexMap forwardIndexMap;
    private AdjacencyMap adjacencyMap;
    private PropertiesMap propertiesMap;

    public Storage(String recordManagerName) throws IOException {
        recordManager = RecordManagerFactory.createRecordManager(recordManagerName);

        // initialize all storage maps
        wordMap = new WordMap(recordManager);
        documentMap = new DocumentMap(recordManager);
        invertedIndexMap = new InvertedIndexMap(recordManager);
        forwardIndexMap = new ForwardIndexMap(recordManager);
        adjacencyMap = new AdjacencyMap(recordManager);
        propertiesMap = new PropertiesMap(recordManager);
    }

    public void commitAndClose() throws IOException {
        documentMap.print();
        propertiesMap.print();
        forwardIndexMap.print();

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

    public boolean docNeedsUpdating(Integer docId, Date newLastModifiedAt) throws IOException {
        Properties properties = propertiesMap.get(docId);
        if (properties != null) {
            return newLastModifiedAt.after(properties.getLastModifiedAt());
        }
        return true;
    }

    public void updateDocument(Integer docId, Properties properties, List<String> words) throws IOException {
        // update properties map
        propertiesMap.put(docId, properties);

        // update forward index map 
        Set<String> uniqueWords = new HashSet<String>(words);
        forwardIndexMap.put(docId, uniqueWords);
    }
}
package storage;

import java.io.IOException;
import java.util.Date;

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
        wordMap.commitAndClose();
        documentMap.commitAndClose();
        invertedIndexMap.commitAndClose();
        forwardIndexMap.commitAndClose();
        adjacencyMap.commitAndClose();
        propertiesMap.commitAndClose();
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
            if (!newLastModifiedAt.after(properties.getLastModifiedAt())) {
                return false;
            }
        }
        return true;
    }

}

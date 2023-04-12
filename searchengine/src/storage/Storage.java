package storage;

import java.io.IOException;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

// the storage class is responsible for managing storage, updating documents, and outputting results
public class Storage {
    private RecordManager recordManager;

    protected WordMap wordMap;
    protected ReverseWordMap reverseWordMap;
    protected DocumentMap documentMap;
    protected ReverseDocumentMap reverseDocumentMap;
    protected TitleInvertedIndexMap titleInvertedIndexMap;
    protected BodyInvertedIndexMap bodyInvertedIndexMap;
    protected TitleForwardIndexMap titleForwardIndexMap;
    protected BodyForwardIndexMap bodyForwardIndexMap;
    protected AdjacencyMap adjacencyMap;
    protected PropertiesMap propertiesMap;

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
}

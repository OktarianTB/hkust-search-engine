package storage;

import java.io.IOException;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * DocumentMap is a HashMap from Document URL => Doc ID
 */
public class DocumentMap extends Map<String, Integer> {
    static final String PAGE_MAP_OBJECT_NAME = "DocumentMap";
    static final String CURRENT_MAX_ID_KEYWORD = "CURRENT_MAX_ID_KEYWORD";

    public DocumentMap(RecordManager recordManager) throws IOException {
        super(recordManager, PAGE_MAP_OBJECT_NAME);

        if (!contains(CURRENT_MAX_ID_KEYWORD)) {
            put(CURRENT_MAX_ID_KEYWORD, 0);
        }
    }

    // Returns the next available ID to use and updates internally
    // Non-deterministic so to be used with caution
    public Integer getNextDocId() throws IOException {
        // get current max ID
        Integer nextDocId = get(CURRENT_MAX_ID_KEYWORD) + 1;

        // update to new max ID
        put(CURRENT_MAX_ID_KEYWORD, nextDocId);

        return nextDocId;
    }

    public void print() throws IOException {
        FastIterator iterator = keys();
        String key = (String) iterator.next();
        while (key != null) {
            System.out.println(key + " -> " + get(key));
            key = (String) iterator.next();
        }
    }
}

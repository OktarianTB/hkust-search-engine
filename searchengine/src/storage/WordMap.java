package storage;

import java.io.IOException;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * WordMap is a HashMap from Word => Word ID
 */
public class WordMap extends Map<String, Integer> {
    static final String PAGE_MAP_OBJECT_NAME = "WordMap";
    static final String CURRENT_MAX_ID_KEYWORD = "CURRENT_MAX_ID_KEYWORD";

    public WordMap(RecordManager recordManager) throws IOException {
        super(recordManager, PAGE_MAP_OBJECT_NAME);

        if (!contains(CURRENT_MAX_ID_KEYWORD)) {
            put(CURRENT_MAX_ID_KEYWORD, 0);
        }
    }

    // Returns the next available ID to use and updates internally
    // Non-deterministic so to be used with caution
    public Integer getNextWordId() throws IOException {
        // get current max ID
        Integer nextWordId = get(CURRENT_MAX_ID_KEYWORD) + 1;

        // update to new max ID
        put(CURRENT_MAX_ID_KEYWORD, nextWordId);

        return nextWordId;
    }

    public void print() throws IOException {
        System.out.println("--- Word Map ---");
        FastIterator iterator = keys();
        String key = (String) iterator.next();
        while (key != null) {
            System.out.println(key + " -> " + get(key));
            key = (String) iterator.next();
        }
    }
}

package storage;

import java.io.IOException;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * WordMap is a HashMap from Word => Word ID
 */
public class WordMap extends BaseMap<String, Integer> {
    static final String MAP_OBJECT_NAME = "WordMap";
    static final String CURRENT_MAX_ID_KEYWORD = "CURRENT_MAX_ID_KEYWORD";

    public WordMap(RecordManager recordManager) throws IOException {
        super(recordManager, MAP_OBJECT_NAME);

        if (!contains(CURRENT_MAX_ID_KEYWORD)) {
            put(CURRENT_MAX_ID_KEYWORD, 0);
        }
    }

    // Returns the next available ID to use and updates internally
    // Non-deterministic so to be used with caution
    public Integer getNextWordId() throws IOException {
        // get next word ID
        Integer nextWordId = get(CURRENT_MAX_ID_KEYWORD);

        // update the new max ID
        put(CURRENT_MAX_ID_KEYWORD, nextWordId + 1);

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

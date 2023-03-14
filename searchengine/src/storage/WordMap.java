package storage;

import java.io.IOException;

import jdbm.helper.FastIterator;
import utilities.Map;

/*
 * WordMap is a HashMap from Page URL => Page ID
 */
public class WordMap extends Map<String, Integer> {
    static final String PAGE_MAP_RECORD_NAME = "WordMap";
    static final String PAGE_MAP_OBJECT_NAME = "WordMap";
    static final String CURRENT_MAX_ID_KEYWORD = "CURRENT_MAX_ID_KEYWORD";

    public WordMap() throws IOException {
        super(PAGE_MAP_RECORD_NAME, PAGE_MAP_OBJECT_NAME);

        if (!contains(CURRENT_MAX_ID_KEYWORD)) {
            put(CURRENT_MAX_ID_KEYWORD, 0);
        }
    }

    // Returns the next available ID to use and updates internally
    // Non-deterministic so to be used with caution
    public Integer getNextId() throws IOException {
        // get current max ID
        Integer nextId = get(CURRENT_MAX_ID_KEYWORD) + 1;

        // update to new max ID
        put(CURRENT_MAX_ID_KEYWORD, nextId);

        return nextId;
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

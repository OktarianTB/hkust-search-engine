package storage;

import java.io.IOException;
import java.util.List;

import jdbm.helper.FastIterator;
import utilities.Map;

/*
 * InvertedIndexMap is a HashMap from Page ID => List of Postings
 */
public class InvertedIndexMap extends Map<Integer, List<Posting>> {
    static final String PAGE_MAP_RECORD_NAME = "InvertedIndexMap";
    static final String PAGE_MAP_OBJECT_NAME = "InvertedIndexMap";

    public InvertedIndexMap() throws IOException {
        super(PAGE_MAP_RECORD_NAME, PAGE_MAP_OBJECT_NAME);
    }

    public void print() throws IOException {
        FastIterator iterator = keys();
        Integer key = (Integer) iterator.next();
        while (key != null) {
            System.out.println(key + " -> " + get(key));
            key = (Integer) iterator.next();
        }
    }
}

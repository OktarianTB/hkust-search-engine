package storage;

import java.io.IOException;
import java.util.List;

import jdbm.helper.FastIterator;
import utilities.Map;

/*
 * ForwardIndexMap is a HashMap from Page ID => Page Keywords (Word IDs)
 */
public class ForwardIndexMap extends Map<Integer, List<String>> {
    static final String PAGE_MAP_RECORD_NAME = "ForwardIndexMap";
    static final String PAGE_MAP_OBJECT_NAME = "ForwardIndexMap";

    public ForwardIndexMap() throws IOException {
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

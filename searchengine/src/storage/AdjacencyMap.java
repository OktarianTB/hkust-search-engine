package storage;

import java.io.IOException;
import java.util.List;

import jdbm.helper.FastIterator;
import utilities.Map;

/*
 * AdjacencyMap is a HashMap from Child Page ID => List of Parent Page IDs
 */
public class AdjacencyMap extends Map<Integer, List<Integer>> {
    static final String PAGE_MAP_RECORD_NAME = "AdjacencyMap";
    static final String PAGE_MAP_OBJECT_NAME = "AdjacencyMap";

    public AdjacencyMap() throws IOException {
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

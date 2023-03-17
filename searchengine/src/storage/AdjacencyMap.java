package storage;

import java.io.IOException;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * AdjacencyMap is a HashMap from Child Doc ID => List of Parent Doc IDs
 */
public class AdjacencyMap extends Map<Integer, Relationship> {
    static final String PAGE_MAP_OBJECT_NAME = "AdjacencyMap";

    public AdjacencyMap(RecordManager recordManager) throws IOException {
        super(recordManager, PAGE_MAP_OBJECT_NAME);
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

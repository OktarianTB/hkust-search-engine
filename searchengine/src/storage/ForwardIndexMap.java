package storage;

import java.io.IOException;
import java.util.Set;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * ForwardIndexMap is a HashMap from Page ID => Page Keywords (Word IDs)
 */
public class ForwardIndexMap extends Map<Integer, Set<String>> {
    static final String PAGE_MAP_OBJECT_NAME = "ForwardIndexMap";

    public ForwardIndexMap(RecordManager recordManager) throws IOException {
        super(recordManager, PAGE_MAP_OBJECT_NAME);
    }

    public void print() throws IOException {
        System.out.println("--- Forward Index Map ---");
        FastIterator iterator = keys();
        Integer key = (Integer) iterator.next();
        while (key != null) {
            System.out.println(key + " -> " + get(key));
            key = (Integer) iterator.next();
        }
    }
}
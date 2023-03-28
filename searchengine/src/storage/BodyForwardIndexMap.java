package storage;

import java.io.IOException;
import java.util.Set;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * BodyForwardIndexMap is a HashMap from Doc ID => Doc Body Keywords (Word IDs)
 */
public class BodyForwardIndexMap extends Map<Integer, Set<Integer>> {
    static final String PAGE_MAP_OBJECT_NAME = "BodyForwardIndexMap";

    public BodyForwardIndexMap(RecordManager recordManager) throws IOException {
        super(recordManager, PAGE_MAP_OBJECT_NAME);
    }

    public void print() throws IOException {
        System.out.println("--- Body Forward Index Map ---");
        FastIterator iterator = keys();
        Integer key = (Integer) iterator.next();
        while (key != null) {
            System.out.println(key + " -> " + get(key));
            key = (Integer) iterator.next();
        }
    }
}

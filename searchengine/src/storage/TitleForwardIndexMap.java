package storage;

import java.io.IOException;
import java.util.Set;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * TitleForwardIndexMap is a HashMap from Doc ID => Doc Title Keywords (Word IDs)
 */
public class TitleForwardIndexMap extends BaseMap<Integer, Set<Integer>> {
    static final String PAGE_MAP_OBJECT_NAME = "TitleForwardIndexMap";

    public TitleForwardIndexMap(RecordManager recordManager) throws IOException {
        super(recordManager, PAGE_MAP_OBJECT_NAME);
    }

    public void print() throws IOException {
        System.out.println("--- Title Forward Index Map ---");
        FastIterator iterator = keys();
        Integer key = (Integer) iterator.next();
        while (key != null) {
            System.out.println(key + " -> " + get(key));
            key = (Integer) iterator.next();
        }
    }
}

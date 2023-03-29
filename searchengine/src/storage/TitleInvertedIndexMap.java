package storage;

import java.io.IOException;
import java.util.Set;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * TitleInvertedIndexMap is a HashMap from Doc ID => Set of Title Postings
 */
public class TitleInvertedIndexMap extends Map<Integer, Set<Posting>> {
    static final String MAP_OBJECT_NAME = "TitleInvertedIndexMap";

    public TitleInvertedIndexMap(RecordManager recordManager) throws IOException {
        super(recordManager, MAP_OBJECT_NAME);
    }

    public void print() throws IOException {
        System.out.println("--- Title Invertex Index Map ---");
        FastIterator iterator = keys();
        Integer key = (Integer) iterator.next();
        while (key != null) {
            System.out.println(key + " -> " + get(key));
            key = (Integer) iterator.next();
        }
    }
}

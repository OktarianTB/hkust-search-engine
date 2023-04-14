package storage;

import java.io.IOException;
import java.util.Map;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * TitleInvertedIndexMap is a HashMap from Word ID => Map of Doc Id => Title Postings
 */
public class TitleInvertedIndexMap extends BaseMap<Integer, Map<Integer, Posting>> {
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

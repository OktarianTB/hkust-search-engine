package storage;

import java.io.IOException;
import java.util.Map;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * BodyInvertedIndexMap is a HashMap from Word ID => Map of Doc Id => Body Postings
 */
public class BodyInvertedIndexMap extends BaseMap<Integer, Map<Integer, Posting>> {
    static final String MAP_OBJECT_NAME = "BodyInvertedIndexMap";

    public BodyInvertedIndexMap(RecordManager recordManager) throws IOException {
        super(recordManager, MAP_OBJECT_NAME);
    }

    public void print() throws IOException {
        System.out.println("--- Body Invertex Index Map ---");
        FastIterator iterator = keys();
        Integer key = (Integer) iterator.next();
        while (key != null) {
            System.out.println(key + " -> " + get(key));
            key = (Integer) iterator.next();
        }
    }
}

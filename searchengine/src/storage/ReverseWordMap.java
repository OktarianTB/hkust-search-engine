package storage;

import java.io.IOException;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * ReverseWordMap is a HashMap from Word ID => Word
 */
public class ReverseWordMap extends Map<Integer, String> {
    static final String MAP_OBJECT_NAME = "ReverseWordMap";

    public ReverseWordMap(RecordManager recordManager) throws IOException {
        super(recordManager, MAP_OBJECT_NAME);
    }
    public void print() throws IOException {
        System.out.println("--- Reverse Word Map ---");
        FastIterator iterator = keys();
        Integer key = (Integer) iterator.next();
        while (key != null) {
            System.out.println(key + " -> " + get(key));
            key = (Integer) iterator.next();
        }
    }
}

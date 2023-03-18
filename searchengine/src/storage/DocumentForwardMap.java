package storage;

import java.io.IOException;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * DocumentForwardMap is a HashMap from Document ID => Doc URL
 */
public class DocumentForwardMap extends Map<Integer, String> {
    static final String PAGE_MAP_OBJECT_NAME = "DocumentForwardMap";

    public DocumentForwardMap(RecordManager recordManager) throws IOException {
        super(recordManager, PAGE_MAP_OBJECT_NAME);
    }

    public void print() throws IOException {
        System.out.println("--- Document Forward Map ---");
        FastIterator iterator = keys();
        Integer key = (Integer) iterator.next();
        while (key != null) {
            System.out.println(key + " -> " + get(key));
            key = (Integer) iterator.next();
        }
        System.out.println();
    }
}

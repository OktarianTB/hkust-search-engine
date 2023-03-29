package storage;

import java.io.IOException;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * PropertiesMap is a HashMap from Doc ID => Document Properties
 */
public class PropertiesMap extends Map<Integer, Properties> {
    static final String MAP_OBJECT_NAME = "PropertiesMap";

    public PropertiesMap(RecordManager recordManager) throws IOException {
        super(recordManager, MAP_OBJECT_NAME);
    }

    public void print() throws IOException {
        System.out.println("--- Properties Map ---");
        FastIterator iterator = keys();
        Integer key = (Integer) iterator.next();
        while (key != null) {
            System.out.println(key + " -> " + get(key));
            key = (Integer) iterator.next();
        }
        System.out.println();
    }
}

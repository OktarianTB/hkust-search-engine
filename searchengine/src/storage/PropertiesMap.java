package storage;

import java.io.IOException;

import jdbm.helper.FastIterator;
import utilities.Map;

/*
 * PropertiesMap is a HashMap from Page ID => Page Properties
 */
public class PropertiesMap extends Map<Integer, Properties> {
    static final String PAGE_MAP_RECORD_NAME = "PropertiesMap";
    static final String PAGE_MAP_OBJECT_NAME = "PropertiesMap";

    public PropertiesMap() throws IOException {
        super(PAGE_MAP_RECORD_NAME, PAGE_MAP_OBJECT_NAME);
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

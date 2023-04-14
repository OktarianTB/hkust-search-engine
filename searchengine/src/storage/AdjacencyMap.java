package storage;

import java.io.IOException;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/*
 * AdjacencyMap is a HashMap from Doc ID => Relationship (List of Child & Parent Doc IDs)
 */
public class AdjacencyMap extends BaseMap<Integer, Relationship> {
    static final String MAP_OBJECT_NAME = "AdjacencyMap";

    public AdjacencyMap(RecordManager recordManager) throws IOException {
        super(recordManager, MAP_OBJECT_NAME);
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

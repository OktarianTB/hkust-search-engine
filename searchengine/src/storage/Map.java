package storage;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;

public class Map<K, V> {
	protected HTree hashTable;

	public Map(RecordManager recordManager, String objectName) throws IOException {
		long recid = recordManager.getNamedObject(objectName);
		if (recid != 0) {
			hashTable = HTree.load(recordManager, recid);
		} else {
			hashTable = HTree.createInstance(recordManager);
			recordManager.setNamedObject(objectName, hashTable.getRecid());
		}
	}

	@SuppressWarnings("unchecked")
	public V get(K key) throws IOException {
		return (V) hashTable.get(key);
	}

	public boolean contains(K key) throws IOException {
		return (hashTable.get(key) != null);
	}

	public void put(K key, V value) throws IOException {
		hashTable.put(key, value);
	}

	public void remove(String key) throws IOException {
		hashTable.remove(key);
	}

	public FastIterator keys() throws IOException {
		return hashTable.keys();
	}
}

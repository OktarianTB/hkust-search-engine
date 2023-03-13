package utilities;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;

public class Map<K, V> {
	private RecordManager recordManager;
	protected HTree hashTable;

	public Map(String recordManagerName, String objectName) throws IOException {
		recordManager = RecordManagerFactory.createRecordManager(recordManagerName);
		long recid = recordManager.getNamedObject(objectName);

		if (recid != 0)
			hashTable = HTree.load(recordManager, recid);
		else {
			hashTable = HTree.createInstance(recordManager);
			recordManager.setNamedObject(objectName, hashTable.getRecid());
		}
	}

	public void finalize() throws IOException {
		recordManager.commit();
		recordManager.close();
	}

	@SuppressWarnings("unchecked")
	public V get(K key) throws IOException {
		return (V)hashTable.get(key);
	}

	public boolean contains(K key) throws IOException {
		return (hashTable.get(key) != null);
	}

	public void put(K key, V value) throws IOException {
		hashTable.put(key, value);
	}

	public FastIterator keys() throws IOException {
		return hashTable.keys();
	}

	public void remove(String key) throws IOException {
		hashTable.remove(key);
	}
}

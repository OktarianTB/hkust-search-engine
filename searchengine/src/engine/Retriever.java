package engine;

import java.io.IOException;

import storage.Storage;

// this class is responsible for retrieving documents from storage (read-only)
class Retriever extends Storage {

    public Retriever(String recordManagerName) throws IOException {
        super(recordManagerName);
    }
  
}

package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import storage.Storage;
import utilities.Result;
import utilities.TextParser;

class Engine {
    private final static String STORAGE_NAME = "search_engine";
    private Storage storage;
    private TextParser textParser;

    public Engine() throws IOException {
        textParser = new TextParser();

        storage = new Storage(STORAGE_NAME);

        storage.commitAndClose();
    }

    public List<Result> search(String query) throws IOException {
        List<String> queryWords = textParser.parseWords(query);

        return new ArrayList<Result>();
    }

    public static void main(String[] args) throws Exception {
        Engine searchEngine = new Engine();
        searchEngine.search("hkust academics");
    }
}

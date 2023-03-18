import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import storage.Storage;
import utilities.Result;

public class App {
    private final static String STORAGE_NAME = "search_engine";
    private final static String OUTPUT_FILE_NAME = "spider_result.txt";
    private Storage storage;

    public App() throws Exception {
        storage = new Storage(STORAGE_NAME);
        outputResults();
        storage.commitAndClose();
    }

    private void outputResults() throws IOException {
        List<Result> results = storage.getResults();

        StringBuilder sb = new StringBuilder();
        for (Result result : results) {
            sb.append(result.toString());
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE_NAME));
        writer.write(sb.toString());
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        new App();
    }
}

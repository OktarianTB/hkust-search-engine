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

    // output results to file
    private void outputResults() throws IOException {
        List<Result> results = storage.getResults();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            Result result = results.get(i);
            sb.append(i + ": " + result.toString());
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE_NAME));
        writer.write(sb.toString());
        writer.close();

        System.out.println("Success - results written to " + OUTPUT_FILE_NAME);
    }

    public static void main(String[] args) throws Exception {
        new App();
    }
}

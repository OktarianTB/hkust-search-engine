import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import crawler.Indexer;
import utilities.Constants;
import utilities.Result;

public class App {
    private final static String OUTPUT_FILE_NAME = "spider_result.txt";
    private Indexer indexer;

    public App() throws Exception {
        indexer = new Indexer(Constants.STORAGE_NAME);
        outputResults();
        indexer.commitAndClose();
    }

    // output results to file
    private void outputResults() throws IOException {
        List<Result> results = indexer.getResults();

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

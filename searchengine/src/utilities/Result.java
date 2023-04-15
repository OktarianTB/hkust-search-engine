package utilities;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import storage.Properties;

// Result class stores the information of a page needed in the test program
public class Result {
    private double score;
    private String url;
    private Properties properties;
    private Map<String, Integer> wordFrequencyMap;
    private List<String> childLinks;

    public Result(double score, String url, Properties properties, Map<String, Integer> wordFrequencyMap, List<String> childLinks) {
        this.score = score;
        this.properties = properties;
        this.url = url;
        this.wordFrequencyMap = wordFrequencyMap;
        this.childLinks = childLinks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[" + String.format("%.3f", score) + "]: " + properties.getTitle() + "\n");
        sb.append(url + "\n");
        sb.append(properties.getLastModifiedAt() + ", " + properties.getSize() + "\n");

        int count = 0;

        Stream<Map.Entry<String, Integer>> sortedWordFrequencyMap = wordFrequencyMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        for (Entry<String, Integer> entry : sortedWordFrequencyMap.collect(java.util.stream.Collectors.toList())) {
            if (count <= 20) {
                String word = entry.getKey();
                sb.append(word + " " + entry.getValue() + "; ");
                count++;
            } else {
                break;
            }
        }
        sb.append("\n");

        for (int i = 0; i < 10 && i < childLinks.size(); i++) {
            sb.append(childLinks.get(i) + "\n");
        }

        sb.append("\n");
        return sb.toString();
    }
}

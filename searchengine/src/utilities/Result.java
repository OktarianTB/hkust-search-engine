package utilities;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import storage.Properties;

// Result class stores the information of a page needed in the test program
public class Result {
    private int docId;
    private double score;
    private String url;
    private Properties properties;
    private Map<String, Integer> wordFrequencyMap;
    private List<String> parentLinks;
    private List<String> childLinks;

    public Result(int docId, double score, String url, Properties properties, Map<String, Integer> wordFrequencyMap,
            List<String> parentLinks, List<String> childLinks) {
        this.docId = docId;
        this.score = score;
        this.properties = properties;
        this.url = url;
        this.wordFrequencyMap = wordFrequencyMap;
        this.parentLinks = parentLinks;
        this.childLinks = childLinks;
    }

    public JsonObject toJson() {
        Map<String, Integer> sortedMap = wordFrequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        Gson gson = new Gson();
        JsonObject result = new JsonObject();
        result.addProperty("docId", docId);
        result.addProperty("score", score);
        result.addProperty("url", url);
        result.add("properties", gson.toJsonTree(properties));
        result.add("wordFrequencyMap", gson.toJsonTree(sortedMap));
        result.add("parentLinks", gson.toJsonTree(parentLinks));
        result.add("childLinks", gson.toJsonTree(childLinks));
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(docId + " - [" + String.format("%.3f", score) + "]: " + properties.getTitle() + "\n");
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

        sb.append("Parent Links:\n");
        for (int i = 0; i < 5 && i < parentLinks.size(); i++) {
            sb.append(parentLinks.get(i) + "\n");
        }

        sb.append("Child Links:\n");
        for (int i = 0; i < 5 && i < childLinks.size(); i++) {
            sb.append(childLinks.get(i) + "\n");
        }

        sb.append("\n");
        return sb.toString();
    }
}

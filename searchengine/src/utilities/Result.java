package utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import storage.Properties;

// Result class stores the information of a page needed in the test program
public class Result {
    private String url;
    private Properties properties;
    private Map<String, Integer> wordFrequencyMap;
    private List<String> childLinks;
    private Map<String, Set<Integer>> titleWordPositions;
    private Map<String, Set<Integer>> bodyWordPositions;

    public Result(String url, Properties properties, Map<String, Integer> wordFrequencyMap, List<String> childLinks,
            Map<String, Set<Integer>> titleWordPositions, Map<String, Set<Integer>> bodyWordPositions) {
        this.properties = properties;
        this.url = url;
        this.wordFrequencyMap = wordFrequencyMap;
        this.childLinks = childLinks;
        this.titleWordPositions = titleWordPositions;
        this.bodyWordPositions = bodyWordPositions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(properties.getTitle() + "\n");
        sb.append(url + "\n");
        sb.append(properties.getLastModifiedAt() + ", " + properties.getSize() + ", "
                + properties.getMaxTitleFrequency() + ", "
                + properties.getMaxBodyFrequency() + "\n");

        int count = 0;
        for (Entry<String, Integer> entry : wordFrequencyMap.entrySet()) {
            if (count <= 10) {
                String word = entry.getKey();
                sb.append(word + " " + entry.getValue() + " " + titleWordPositions.getOrDefault(word, new HashSet<>()) + " "
                        + bodyWordPositions.getOrDefault(word, new HashSet<>()) + "; ");
                count++;
            } else {
                break;
            }
        }
        sb.append("\n");

        for (int i = 0; i <= 10 && i < childLinks.size(); i++) {
            sb.append(childLinks.get(i).toString() + "\n");
        }

        sb.append("\n");
        return sb.toString();
    }
}

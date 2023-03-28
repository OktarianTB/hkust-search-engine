package utilities;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Result {
    private String title;
    private String url;
    private int size;
    private Date lastModifiedAt;
    private Map<String, Integer> wordFrequencyMap;
    private List<String> childLinks;

    public Result(String title, String url, int size, Date lastModifiedAt, Map<String, Integer> wordFrequencyMap,
            List<String> childLinks) {
        this.title = title;
        this.url = url;
        this.size = size;
        this.lastModifiedAt = lastModifiedAt;
        this.wordFrequencyMap = wordFrequencyMap;
        this.childLinks = childLinks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(title + "\n");
        sb.append(url + "\n");
        sb.append(lastModifiedAt + ", " + size + "\n");

        int count = 0;
        for (Entry<String, Integer> entry : wordFrequencyMap.entrySet()) {
            if (count <= 10) {
                sb.append(entry.getKey() + " " + entry.getValue() + "; ");
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

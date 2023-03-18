package utilities;

import java.util.Date;
import java.util.List;

public class Result {
    private String title;
    private String url;
    private int size;
    private Date lastModifiedAt;
    private List<Pair> pairs;
    private List<String> childLinks;

    public Result(String title, String url, int size, Date lastModifiedAt, List<Pair> pairs, List<String> childLinks) {
        this.title = title;
        this.url = url;
        this.size = size;
        this.lastModifiedAt = lastModifiedAt;
        this.pairs = pairs;
        this.childLinks = childLinks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(title + "\n");
        sb.append(url+ "\n");
        sb.append(lastModifiedAt + ", " + size + "\n");

        for (int i = 0; i <= 10 && i < pairs.size(); i++) {
            sb.append(pairs.get(i).toString());
        }
        sb.append("\n");

        for (int i = 0; i <= 10 && i < childLinks.size(); i++) {
            sb.append(childLinks.get(i).toString() + "\n");
        }

        sb.append("\n");
        return sb.toString();
    }
}

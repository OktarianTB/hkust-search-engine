package crawler;

import java.util.Date;
import java.util.List;

import storage.Properties;

class Page {
    private String url;
    private String title;
    private String text;
    private List<String> links;
    private int size;
    private Date lastModifiedAt;

    Page(String url, String title, String text, List<String> links, int size, Date lastModifiedAt) {
        this.url = url;
        this.title = title;
        this.text = text;
        this.links = links;
        this.size = size;
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getUrl() {
        return this.url;
    }

    public String getTitle() {
        return this.title;
    }

    public String getText() {
        return this.text;
    }

    public List<String> getLinks() {
        return this.links;
    }

    public int getSize() {
        return this.size;
    }

    public Date getLastModifiedAt() {
        return this.lastModifiedAt;
    }

    public Properties toProperties() {
        return new Properties(url, title, size, lastModifiedAt);
    }

    @Override
    public String toString() {
        return "{" +
            " url='" + getUrl() + "'" +
            ", title='" + getTitle() + "'" +
            ", text='" + getText() + "'" +
            ", links='" + getLinks() + "'" +
            ", size='" + getSize() + "'" +
            ", lastModifiedAt='" + getLastModifiedAt() + "'" +
            "}";
    }    
}

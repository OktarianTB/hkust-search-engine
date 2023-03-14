package storage;

import java.util.Date;

public class Properties {
    private String url;
    private String title;
    private int size;
    private Date lastModifiedAt;

    public Properties(String url, String title, int size, Date lastModifiedAt) {
        this.url = url;
        this.title = title;
        this.size = size;
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getUrl() {
        return this.url;
    }

    public String getTitle() {
        return this.title;
    }

    public int getSize() {
        return this.size;
    }

    public Date getLastModifiedAt() {
        return this.lastModifiedAt;
    }

    @Override
    public String toString() {
        return "{" +
                " url='" + getUrl() + "'" +
                ", title='" + getTitle() + "'" +
                ", size='" + getSize() + "'" +
                ", lastModifiedAt='" + getLastModifiedAt() + "'" +
                "}";
    }
}
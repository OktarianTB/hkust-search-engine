package storage;

import java.io.Serializable;
import java.util.Date;

public class Properties implements Serializable {
    private String title;
    private int size;
    private Date lastModifiedAt;
    private int maxTitleFrequency;
    private int maxBodyFrequency;

    public Properties(String title, int size, Date lastModifiedAt, int maxTitleFrequency, int maxBodyFrequency) {
        this.title = title;
        this.size = size;
        this.lastModifiedAt = lastModifiedAt;
        this.maxTitleFrequency = maxTitleFrequency;
        this.maxBodyFrequency = maxBodyFrequency;
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

    public int getMaxTitleFrequency() {
        return this.maxTitleFrequency;
    }

    public int getMaxBodyFrequency() {
        return this.maxBodyFrequency;
    }

    @Override
    public String toString() {
        return "{" +
                " title='" + getTitle() + "'" +
                ", size='" + getSize() + "'" +
                ", lastModifiedAt='" + getLastModifiedAt() + "'" +
                ", maxTitleFrequency='" + getMaxTitleFrequency() + "'" +
                ", maxBodyFrequency='" + getMaxBodyFrequency() + "'" +
                "}";
    }
}
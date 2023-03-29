package storage;

import java.io.Serializable;
import java.util.Date;

public class Properties implements Serializable {
    private String title;
    private int size;
    private Date lastModifiedAt;

    public Properties(String title, int size, Date lastModifiedAt) {
        this.title = title;
        this.size = size;
        this.lastModifiedAt = lastModifiedAt;
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
                " title='" + getTitle() + "'" +
                ", size='" + getSize() + "'" +
                ", lastModifiedAt='" + getLastModifiedAt() + "'" +
                "}";
    }
}
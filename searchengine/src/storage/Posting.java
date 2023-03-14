package storage;

public class Posting {
    private Integer pageId;
    private int frequency;

    public Posting(Integer pageId, int frequency) {
        this.pageId = pageId;
        this.frequency = frequency;
    }

    public Integer getPageId() {
        return this.pageId;
    }

    public int getFrequency() {
        return this.frequency;
    }

    @Override
    public String toString() {
        return "{" +
                " pageId='" + getPageId() + "'" +
                ", frequency='" + getFrequency() + "'" +
                "}";
    }
}

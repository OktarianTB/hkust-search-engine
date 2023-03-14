package storage;

public class Posting {
    private Integer docId;
    private int frequency;

    public Posting(Integer docId, int frequency) {
        this.docId = docId;
        this.frequency = frequency;
    }

    public Integer getDocId() {
        return this.docId;
    }

    public int getFrequency() {
        return this.frequency;
    }

    @Override
    public String toString() {
        return "{" +
                " docId='" + getDocId() + "'" +
                ", frequency='" + getFrequency() + "'" +
                "}";
    }
}

package utilities;

public class Pair {
    private String word;
    private Integer frequency;

    public Pair(String word, Integer frequency) {
        this.word = word;
        this.frequency = frequency;
    }

    public String getWord() {
        return this.word;
    }

    public Integer getFrequency() {
        return this.frequency;
    }

    @Override
    public String toString() {
        return getWord() + " " + getFrequency() + "; ";
    }
}
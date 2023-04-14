package storage;

import java.io.Serializable;
import java.util.Set;

public class Posting implements Serializable {
    private int frequency;
    private Set<Integer> positions;

    public Posting(int frequency, Set<Integer> positions) {
        this.frequency = frequency;
        this.positions = positions;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public Set<Integer> getPositions() {
        return this.positions;
    }

    @Override
    public String toString() {
        return "{" +
                ", frequency='" + getFrequency() + "'" +
                ", positions='" + getPositions() + "'" +
                "}";
    }
}

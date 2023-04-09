package storage;

import java.io.Serializable;
import java.util.Set;

public class Posting implements Serializable {
    private Integer docId;
    private int frequency;
    private Set<Integer> positions;

    public Posting(Integer docId, int frequency, Set<Integer> positions) {
        this.docId = docId;
        this.frequency = frequency;
        this.positions = positions;
    }

    public Integer getDocId() {
        return this.docId;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public Set<Integer> getPositions() {
        return this.positions;
    }

    @Override
    public int hashCode() {
        return docId;
    }

    @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Posting other = (Posting) obj;
      if (docId != other.docId || frequency != other.frequency || !positions.equals(other.positions))
         return false;
      return true;
   }

    @Override
    public String toString() {
        return "{" +
                " docId='" + getDocId() + "'" +
                ", frequency='" + getFrequency() + "'" +
                ", positions='" + getPositions() + "'" +
                "}";
    }
}

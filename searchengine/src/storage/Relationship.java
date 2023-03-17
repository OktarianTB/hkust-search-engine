package storage;

import java.io.Serializable;
import java.util.Set;

public class Relationship implements Serializable {
    private Set<Integer> parentDocIds;
    private Set<Integer> childDocIds;

    public Relationship(Set<Integer> parentDocIds, Set<Integer> childDocIds) {
        this.parentDocIds = parentDocIds;
        this.childDocIds = childDocIds;
    }

    public Set<Integer> getParentDocIds() {
        return this.parentDocIds;
    }

    public Set<Integer> getChildDocIds() {
        return this.childDocIds;
    }

    @Override
    public String toString() {
        return "{" +
                " parentDocIds='" + getParentDocIds() + "'" +
                ", childDocIds='" + getChildDocIds() + "'" +
                "}";
    }
}

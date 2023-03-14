package storage;

import java.util.List;

public class Relationship {
    private List<Integer> parents;
    private List<Integer> children;

    public Relationship(List<Integer> parents, List<Integer> children) {
        this.parents = parents;
        this.children = children;
    }

    private List<Integer> getParents() {
        return this.parents;
    }

    private List<Integer> getChildren() {
        return this.children;
    }

    @Override
    public String toString() {
        return "{" +
                " parents='" + getParents() + "'" +
                ", children='" + getChildren() + "'" +
                "}";
    }
}

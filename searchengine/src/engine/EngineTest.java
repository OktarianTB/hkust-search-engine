package engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import storage.Posting;

public class EngineTest {
    @Test
    public void testfilterDocuments() {
        Map<Integer, Set<Integer>> documentPositionsMap = new HashMap<>();
        documentPositionsMap.put(1, Set.of(10, 23, 300));
        documentPositionsMap.put(2, Set.of(24, 44, 202, 301));
        documentPositionsMap.put(3, Set.of(44, 100, 150));

        Map<Integer, Posting> nextPostings1 = new HashMap<>();
        nextPostings1.put(1, new Posting(1, Set.of(15, 24)));
        nextPostings1.put(2, new Posting(1, Set.of(45, 203)));
        nextPostings1.put(5, new Posting(1, Set.of(1, 2, 3)));

        Map<Integer, Set<Integer>> filteredPostings = Engine.filterDocuments(documentPositionsMap, nextPostings1);
        Assert.assertEquals(2, filteredPostings.size());
        Assert.assertEquals(Set.of(24), filteredPostings.get(1));
        Assert.assertEquals(Set.of(45, 203), filteredPostings.get(2));

        Map<Integer, Posting> nextPostings2 = new HashMap<>();
        nextPostings2.put(1, new Posting(1, Set.of(26, 24)));
        nextPostings2.put(2, new Posting(1, Set.of(40, 204)));

        filteredPostings = Engine.filterDocuments(filteredPostings, nextPostings2);
        Assert.assertEquals(1, filteredPostings.size());
        Assert.assertEquals(Set.of(204), filteredPostings.get(2));
    }
}

package engine;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import storage.Posting;
import utilities.Tokenizer;

public class EngineTest {
    @Mock
    private Tokenizer tokenizer;

    @Mock
    private Retriever retriever;

    @InjectMocks
    private Engine engine;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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

    @Test
    public void testGetRelevantDocumentForPhraseInTitle() throws IOException {
        List<Integer> queryWordIds = List.of(1, 3, 4);

        // set up mock behavior for retriever

        // word id 1 appears in 3 documents (0, 1, 2)
        Map<Integer, Posting> titlePostings1 = new HashMap<>() {
            {
                put(0, new Posting(1, Set.of(1)));
                put(1, new Posting(1, Set.of(5, 16)));
                put(2, new Posting(1, Set.of(1, 22, 45)));
            }
        };
        when(retriever.getTitlePostings(1)).thenReturn(titlePostings1);

        // word id 3 appears in 4 documents (0, 1, 2, 3)
        Map<Integer, Posting> titlePostings3 = new HashMap<>() {
            {
                put(0, new Posting(1, Set.of(2, 10)));
                put(1, new Posting(1, Set.of(4, 7, 17)));
                put(2, new Posting(1, Set.of(46, 67)));
                put(3, new Posting(1, Set.of(2)));
            }
        };
        when(retriever.getTitlePostings(3)).thenReturn(titlePostings3);

        // word id 4 appears in 2 documents (1, 2)
        Map<Integer, Posting> titlePostings4 = new HashMap<>() {
            {
                put(1, new Posting(1, Set.of(8, 18)));
                put(2, new Posting(1, Set.of(55)));
            }
        };
        when(retriever.getTitlePostings(4)).thenReturn(titlePostings4);

        // test
        Set<Integer> relevantDocuments = engine.getRelevantDocumentForPhraseInTitle(queryWordIds);

        // assert
        assertEquals(1, relevantDocuments.size());
        assertEquals(Set.of(1), relevantDocuments);
    }

    @Test
    public void testGetRelevantDocumentForPhraseInBody() throws IOException {
        List<Integer> queryWordIds = List.of(1, 3, 4);

        // set up mock behavior for retriever

        // word id 1 appears in 3 documents (0, 1, 2)
        Map<Integer, Posting> titlePostings1 = new HashMap<>() {
            {
                put(0, new Posting(1, Set.of(1)));
                put(1, new Posting(1, Set.of(5, 16)));
                put(2, new Posting(1, Set.of(1, 22, 45)));
            }
        };
        when(retriever.getBodyPostings(1)).thenReturn(titlePostings1);

        // word id 3 appears in 4 documents (0, 1, 2, 3)
        Map<Integer, Posting> titlePostings3 = new HashMap<>() {
            {
                put(0, new Posting(1, Set.of(2, 10)));
                put(1, new Posting(1, Set.of(4, 7, 17)));
                put(2, new Posting(1, Set.of(46, 67)));
                put(3, new Posting(1, Set.of(2)));
            }
        };
        when(retriever.getBodyPostings(3)).thenReturn(titlePostings3);

        // word id 4 appears in 2 documents (1, 2)
        Map<Integer, Posting> titlePostings4 = new HashMap<>() {
            {
                put(1, new Posting(1, Set.of(8, 18)));
                put(2, new Posting(1, Set.of(55)));
            }
        };
        when(retriever.getBodyPostings(4)).thenReturn(titlePostings4);

        // test
        Set<Integer> relevantDocuments = engine.getRelevantDocumentForPhraseInBody(queryWordIds);

        // assert
        assertEquals(1, relevantDocuments.size());
        assertEquals(Set.of(1), relevantDocuments);
    }

    @Test
    public void testCalculateQueryVector() throws IOException {
        List<Integer> queryWordIds = List.of(1, 3, 4, 1);
        int vocabularySize = 5;
        int numberOfDocs = 3;

        // set up mock behavior for retriever

        // word id 1 appears in 2 documents (0, 1)
        Map<Integer, Posting> titlePostings1 = new HashMap<>() {
            {
                put(0, new Posting(1, Set.of(1)));
            }
        };
        Map<Integer, Posting> bodyPostings1 = new HashMap<>() {
            {
                put(0, new Posting(3, Set.of(1, 2, 3)));
                put(1, new Posting(3, Set.of(1, 2, 3)));
            }
        };
        when(retriever.getTitlePostings(1)).thenReturn(titlePostings1);
        when(retriever.getBodyPostings(1)).thenReturn(bodyPostings1);

        // word id 3 appears in 3 documents (0, 1, 2)
        Map<Integer, Posting> titlePostings3 = new HashMap<>() {
            {
                put(0, new Posting(1, Set.of(1)));
            }
        };
        Map<Integer, Posting> bodyPostings3 = new HashMap<>() {
            {
                put(1, new Posting(3, Set.of(1, 2, 3)));
                put(2, new Posting(3, Set.of(1, 2, 3)));
            }
        };
        when(retriever.getTitlePostings(3)).thenReturn(titlePostings3);
        when(retriever.getBodyPostings(3)).thenReturn(bodyPostings3);

        // word id 4 appears in 2 documents (1, 2)
        Map<Integer, Posting> titlePostings4 = new HashMap<>() {
            {
                put(1, new Posting(1, Set.of(1)));
            }
        };
        Map<Integer, Posting> bodyPostings4 = new HashMap<>() {
            {
                put(2, new Posting(3, Set.of(1, 2, 3)));
            }
        };
        when(retriever.getTitlePostings(4)).thenReturn(titlePostings4);
        when(retriever.getBodyPostings(4)).thenReturn(bodyPostings4);

        // expected result
        double tf_idf_1 = TfIdf.normalizeTermWeighting(TfIdf.calculateTermWeighting(2, 2, numberOfDocs), 2);
        double tf_idf_3 = TfIdf.normalizeTermWeighting(TfIdf.calculateTermWeighting(1, 3, numberOfDocs), 2);
        double tf_idf_4 = TfIdf.normalizeTermWeighting(TfIdf.calculateTermWeighting(1, 2, numberOfDocs), 2);
        double[] expected = { 0.0, tf_idf_1, 0.0, tf_idf_3, tf_idf_4 };

        // test
        double[] queryVector = engine.calculateQueryVector(queryWordIds, vocabularySize, numberOfDocs);

        // assert
        assertArrayEquals(expected, queryVector, 0.001);
    }
}

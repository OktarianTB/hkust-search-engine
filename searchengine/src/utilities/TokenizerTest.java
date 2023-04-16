package utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class TokenizerTest {
    @Test
    public void testTokenizeText() {
        Tokenizer tokenizer = new Tokenizer();
        String text = "The quick brown fox jumps over the lazy dog!!";
        List<String> words = tokenizer.tokenizeText(text);

        assertEquals(6, words.size());
        assertTrue(words.contains("quick"));
        assertTrue(words.contains("brown"));
        assertTrue(words.contains("fox"));
        assertTrue(words.contains("jump"));
        assertTrue(words.contains("lazi"));
        assertTrue(words.contains("dog"));
    }

    @Test
    public void testTokenizeQuery1() {
        Tokenizer tokenizer = new Tokenizer();
        String query = "The quick \"brown fox\" jumps \"over\" \"the\" \"lazy dog.\"";
        List<Token> tokens = tokenizer.tokenizeQuery(query);

        assertEquals(4, tokens.size());
        assertEquals("quick", tokens.get(0).getWords().get(0));
        assertEquals("brown", tokens.get(1).getWords().get(0));
        assertEquals("fox", tokens.get(1).getWords().get(1));
        assertEquals("jump", tokens.get(2).getWords().get(0));
        assertEquals("lazi", tokens.get(3).getWords().get(0));
        assertEquals("dog", tokens.get(3).getWords().get(1));
    }

    @Test
    public void testTokenizeQuery2() {
        Tokenizer tokenizer = new Tokenizer();
        String query = "\"The quick \"brOWN\" crazy fox\" jumps \"over \"the\" \"lazy dog.";
        List<Token> tokens = tokenizer.tokenizeQuery(query);

        assertEquals(6, tokens.size());
        assertEquals("quick", tokens.get(0).getWords().get(0));
        assertEquals("brown", tokens.get(1).getWords().get(0));
        assertEquals("crazi", tokens.get(2).getWords().get(0));
        assertEquals("fox", tokens.get(2).getWords().get(1));
        assertEquals("jump", tokens.get(3).getWords().get(0));
        assertEquals("lazi", tokens.get(4).getWords().get(0));
        assertEquals("dog", tokens.get(5).getWords().get(0));
    }

    @Test
    public void testTokenizeQuery3() {
        Tokenizer tokenizer = new Tokenizer();
        String query = "The \"over\" \"\"\"\" \"the\" monkey";
        List<Token> tokens = tokenizer.tokenizeQuery(query);

        assertEquals(1, tokens.size());
        assertEquals("monkei", tokens.get(0).getWords().get(0));
    }
}

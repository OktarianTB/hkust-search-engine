package utilities;

import java.util.ArrayList;
import java.util.List;

public class Token {
    private List<String> words;

    public Token() {
        this.words = new ArrayList<String>();
    }

    public void addWord(String word) {
        words.add(word);
    }

    public List<String> getWords() {
        return words;
    }

    public boolean isPhrase() {
        return words.size() > 1;
    }
}

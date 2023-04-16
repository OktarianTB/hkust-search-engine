package engine;

import java.util.List;

public class SearchToken {
    private List<Integer> wordIds;

    public SearchToken(List<Integer> wordIds) {
        this.wordIds = wordIds;
    }

    public List<Integer> getWordIds() {
        return wordIds;
    }

    public boolean isPhrase() {
        return wordIds.size() > 1;
    }
}

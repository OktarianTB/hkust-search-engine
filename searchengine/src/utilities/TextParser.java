package utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TextParser {
    private PorterStemmer stemmer;
    private StopWordsChecker stopWords;

    public TextParser() {
        stemmer = new PorterStemmer();
        stopWords = new StopWordsChecker();
    }

    // transform text into a list of stemmed words
    public List<String> parseWords(String text) {
        List<String> words = new ArrayList<String>();
        StringTokenizer stringTokenizer = new StringTokenizer(text);

        while (stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken().toLowerCase();

            // ignore stop words
            if (stopWords.isStopWord(word) || word.length() == 0) {
                continue;
            }

            // Stem word and add to output list
            String stemmedWord = stemmer.stem(word);
            if (stemmedWord.length() > 0) {
                words.add(stemmedWord);
            }
        }

        return words;
    }
}

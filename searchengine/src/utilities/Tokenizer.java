package utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Tokenizer {
    private PorterStemmer stemmer;
    private StopWordsChecker stopWords;

    public Tokenizer() {
        stemmer = new PorterStemmer();
        stopWords = new StopWordsChecker();
    }

    // tokenize text into a list of stemmed words
    public List<String> tokenizeText(String text) {
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

    // tokenize query text into a list of tokens (stemmed words) for searching
    public List<Token> tokenizeQuery(String query) {
        List<Token> tokens = new ArrayList<Token>();

        int i = 0;

        while (i < query.length()) {
            char c = query.charAt(i);

            if (Character.isWhitespace(c)) {
                // skip whitespace
                i++;
            } else if (c == '"') {
                // quoted string
                int j = i + 1;
                while (j < query.length() && query.charAt(j) != '"') {
                    j++;
                }
                if (j < query.length() && query.charAt(j) == '"') {
                    List<String> words = tokenizeText(query.substring(i + 1, j));

                    if (words.size() > 0) {
                        Token newToken = new Token();
                        for (String word : words) {
                            newToken.addWord(word);
                        }
                        tokens.add(newToken);
                    }

                    i = j + 1;
                } else {
                    // error: unmatched quote
                    throw new IllegalArgumentException("Unmatched quote at position " + i);
                }
            } else {
                // unquoted string
                int j = i + 1;
                while (j < query.length() && !Character.isWhitespace(query.charAt(j)) && query.charAt(j) != '"') {
                    j++;
                }

                Token newToken = new Token();
                String word = query.substring(i, j).toLowerCase();
                i = j;

                // ignore stop words
                if (stopWords.isStopWord(word) || word.length() == 0) {
                    continue;
                }

                // Stem word and add to output list
                String stemmedWord = stemmer.stem(word);
                if (stemmedWord.length() > 0) {
                    newToken.addWord(stemmedWord);
                    tokens.add(newToken);
                }
            }
        }

        return tokens;
    }
}

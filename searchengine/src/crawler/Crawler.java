package crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;

import org.htmlparser.util.ParserException;

import storage.Storage;
import utilities.PorterStemmer;
import utilities.StopWordsChecker;

public class Crawler {
    private final static String STORAGE_NAME = "search_engine";

    private int maxPagesToCrawl;

    private PorterStemmer stemmer;
    private StopWordsChecker stopWords;

    private Queue<String> urlsToVisit;
    private Set<String> visitedUrls;

    private Storage storage;

    Crawler(String startUrl, int maxPagesToCrawl) throws IOException {
        this.maxPagesToCrawl = maxPagesToCrawl;

        stemmer = new PorterStemmer();
        stopWords = new StopWordsChecker();

        urlsToVisit = new LinkedList<String>();
        urlsToVisit.add(startUrl);

        visitedUrls = new HashSet<String>();

        // initialize all storage maps
        storage = new Storage(STORAGE_NAME);

        // crawl and index
        crawlAndIndex();

        // commit db changes
        storage.commitAndClose();
    }

    private void crawlAndIndex() throws IOException {
        int pagesCrawled = 0;

        while (pagesCrawled < maxPagesToCrawl && urlsToVisit.size() > 0) {
            String url = urlsToVisit.remove();
            Page page = getPage(url);

            if (page != null) {
                System.out.println(url + ":\n" + page.getTitle() + "\n\n");

                pagesCrawled += 1;
                visitedUrls.add(url);

                Integer docId = storage.getDocId(url);

                if (storage.docNeedsUpdating(docId, page.getLastModifiedAt())) {
                    List<String> titleWords = getTransformedWords(page.getTitle());
                    List<String> bodyWords = getTransformedWords(page.getText());

                    storage.updateDocument(docId, page.toProperties(), titleWords, bodyWords);
                }

                Set<Integer> linksDocIds = new HashSet<Integer>();
                for (String link : page.getLinks()) {
                    linksDocIds.add(storage.getDocId(link));

                    if (!visitedUrls.contains(link)) {
                        urlsToVisit.add(link);
                    }
                }
                storage.updateRelationships(docId, linksDocIds);
            }
        }
    }

    private List<String> getTransformedWords(String text) {
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

    private Page getPage(String url) {
        try {
            PageParser pageParser = new PageParser(url);
            return pageParser.fetchPage();
        } catch (ParserException e) {
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        new Crawler("https://cse.hkust.edu.hk/", 30);
    }
}

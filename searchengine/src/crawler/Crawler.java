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
    }

    public void crawlAndIndex() throws IOException {
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
                    List<String> words = getTransformedWords(page);
                    storage.updateDocument(docId, page.toProperties(), words);
                }

                for (String link : page.getLinks()) {
                    // todo: update adjacency list
                    if (!visitedUrls.contains(link)) {
                        urlsToVisit.add(link);
                    }
                }
            }
        }

        storage.commitAndClose();
    }

    private List<String> getTransformedWords(Page page) {
        List<String> words = new ArrayList<String>();
        StringTokenizer stringTokenizer = new StringTokenizer(page.getText());

		while (stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken().toLowerCase();

            // ignore stop words
            if (stopWords.isStopWord(word)) {
                continue;
            }

            // Stem word and add to output list
			words.add(stemmer.stem(word));
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
        Crawler crawler = new Crawler("https://cse.hkust.edu.hk", 5);
        crawler.crawlAndIndex();
    }
}

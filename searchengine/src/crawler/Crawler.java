package crawler;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.htmlparser.util.ParserException;

import storage.Storage;
// import utilities.PorterStemmer;
// import utilities.StopWordsChecker;

public class Crawler {
    private final static String STORAGE_NAME = "search_engine";

    private int maxPagesToCrawl;

    // private PorterStemmer porter;
    // private StopWordsChecker stopWords;

    private Queue<String> urlsToVisit;
    private Set<String> visitedUrls;

    private Storage storage;

    Crawler(String startUrl, int maxPagesToCrawl) throws IOException {
        this.maxPagesToCrawl = maxPagesToCrawl;

        // porter = new PorterStemmer();
        // stopWords = new StopWordsChecker();

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
                    storage.updateDocument(docId, page.toProperties());
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

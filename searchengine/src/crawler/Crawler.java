package crawler;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.htmlparser.util.ParserException;

import storage.Storage;
import utilities.TextParser;

// the crawler class is responsible for crawling and indexing pages
public class Crawler {
    private final static String STORAGE_NAME = "search_engine";

    private final static int MAX_PAGES_TO_CRAWL = 30;

    private TextParser textParser;

    private int maxPagesToCrawl;

    private Queue<String> urlsToVisit;
    private Set<String> visitedUrls;

    private Storage storage;

    Crawler(String startUrl, int maxPagesToCrawl) throws IOException {
        this.maxPagesToCrawl = maxPagesToCrawl;

        textParser = new TextParser();

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

    // crawl and index pages
    private void crawlAndIndex() throws IOException {
        int pagesCrawled = 0;

        while (pagesCrawled < maxPagesToCrawl && urlsToVisit.size() > 0) {
            String url = urlsToVisit.remove();

            if (visitedUrls.contains(url)) {
                continue;
            }

            visitedUrls.add(url);
            Page page = getPage(url);

            if (page != null) {
                System.out.println(pagesCrawled + ": " + url + "\n" + page.getTitle());

                Integer docId = storage.getDocId(url);

                Set<Integer> childDocIds = new HashSet<Integer>();
                for (String link : page.getLinks()) {
                    Integer childDocId = storage.getDocId(link);
                    childDocIds.add(childDocId);

                    urlsToVisit.add(link);
                }

                if (storage.docNeedsUpdating(docId, page.getLastModifiedAt())) {
                    List<String> titleWords = textParser.parseWords(page.getTitle());
                    List<String> bodyWords = textParser.parseWords(page.getText());

                    storage.updateDocument(docId, page, titleWords, bodyWords);
                    storage.updateRelationships(docId, childDocIds);
                }

                pagesCrawled += 1;
            }
        }
    }

    // fetch a page from the given url
    private Page getPage(String url) {
        try {
            PageParser pageParser = new PageParser(url);
            return pageParser.fetchPage();
        } catch (ParserException e) {
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        new Crawler("https://cse.hkust.edu.hk/", MAX_PAGES_TO_CRAWL);
    }
}

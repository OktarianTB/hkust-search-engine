package crawler;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.htmlparser.util.ParserException;

import utilities.Constants;
import utilities.TextParser;

// the crawler class is responsible for crawling and indexing pages
public class Crawler {
    private final static int MAX_PAGES_TO_CRAWL = 30;

    private TextParser textParser;

    private int maxPagesToCrawl;

    private Queue<String> urlsToVisit;
    private Set<String> visitedUrls;

    private Indexer indexer;

    Crawler(String startUrl, int maxPagesToCrawl) throws IOException {
        this.maxPagesToCrawl = maxPagesToCrawl;

        textParser = new TextParser();

        urlsToVisit = new LinkedList<String>();
        urlsToVisit.add(startUrl);

        visitedUrls = new HashSet<String>();

        // initialize all storage maps
        indexer = new Indexer(Constants.STORAGE_NAME);

        // crawl and index
        crawlAndIndex();

        // commit db changes
        indexer.commitAndClose();
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
                System.out.println(pagesCrawled + ": " + url + "\n" + page.getTitle() + "\n");

                Integer docId = indexer.getDocId(url);

                Set<Integer> childDocIds = new HashSet<Integer>();
                for (String link : page.getLinks()) {
                    Integer childDocId = indexer.getDocId(link);
                    childDocIds.add(childDocId);

                    urlsToVisit.add(link);
                }

                if (indexer.docNeedsUpdating(docId, page.getLastModifiedAt())) {
                    List<String> titleWords = textParser.parseWords(page.getTitle());
                    List<String> bodyWords = textParser.parseWords(page.getText());

                    indexer.updateDocument(docId, page, titleWords, bodyWords);
                    indexer.updateRelationships(docId, childDocIds);
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
        new Crawler("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", MAX_PAGES_TO_CRAWL);
    }
}

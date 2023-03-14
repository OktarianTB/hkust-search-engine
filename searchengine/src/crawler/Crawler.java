package crawler;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.htmlparser.util.ParserException;

import storage.PageMap;
import utilities.PorterStemmer;
import utilities.StopWordsChecker;

public class Crawler {
    private int maxPagesToCrawl;

    private PorterStemmer porter;
    private StopWordsChecker stopWords;

    private Queue<String> urlsToVisit;
    private Set<String> visitedUrls;

    private PageMap pageMap;

    Crawler(String startUrl, int maxPagesToCrawl) throws IOException {
        this.maxPagesToCrawl = maxPagesToCrawl;

        porter = new PorterStemmer();
        stopWords = new StopWordsChecker();

        urlsToVisit = new LinkedList<String>();
        urlsToVisit.add(startUrl);

        visitedUrls = new HashSet<String>();

        pageMap = new PageMap();
    }

    public void crawl() throws IOException {
        int pagesCrawled = 0;

        while (pagesCrawled < maxPagesToCrawl && urlsToVisit.size() > 0) {
            String nextUrl = urlsToVisit.remove();
            Page page = getPage(nextUrl);

            if (page != null) {
                System.out.println(nextUrl + ":\n" + page.getTitle() + "\n\n");

                pagesCrawled += 1;
                visitedUrls.add(nextUrl);

                if (!pageMap.contains(nextUrl)) {
                    Integer pageId = pageMap.getNextId();
                    pageMap.put(nextUrl, pageId);
                }

                for (String link : page.getLinks()) {
                    if (!visitedUrls.contains(link)) {
                        urlsToVisit.add(link);
                    }
                }
            }
        }

        pageMap.print();
        finalize();
    }

    private Page getPage(String url) {
        try {
            PageParser pageParser = new PageParser(url);
            return pageParser.fetchPage();
        } catch (ParserException e) {
            return null;
        }
    }

    public void finalize() throws IOException {
        pageMap.finalize();
    }

    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler("https://cse.hkust.edu.hk", 5);
        crawler.crawl();
    }
}

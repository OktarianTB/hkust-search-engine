package crawler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.htmlparser.util.ParserException;

import utilities.PorterStemmer;
import utilities.StopWordsChecker;

public class Crawler {
    private int maxPagesToCrawl;

    private PorterStemmer porter;
    private StopWordsChecker stopWords;

    private Queue<String> urlsToVisit;
    private Set<String> visitedUrls;

    Crawler(String startUrl, int maxPagesToCrawl) {
        this.maxPagesToCrawl = maxPagesToCrawl;

        porter = new PorterStemmer();
        stopWords = new StopWordsChecker();

        urlsToVisit = new LinkedList<String>();
        urlsToVisit.add(startUrl);

        visitedUrls = new HashSet<String>();
    }

    public void crawl() {
        int pagesCrawled = 0;

        while(pagesCrawled < maxPagesToCrawl && urlsToVisit.size() > 0) {
            String nextUrl = urlsToVisit.remove();
            Page page = getPage(nextUrl);

            if (page != null) {
                System.out.println(nextUrl + ":\n" + page.getTitle() + "\n\n");

                pagesCrawled += 1;
                visitedUrls.add(nextUrl);

                for (String link : page.getLinks()) {
                    if (!visitedUrls.contains(link)) {
                        urlsToVisit.add(link);
                    }
                }
            }
        }
    }

    private Page getPage(String url) {
        try {
            PageParser pageParser = new PageParser(url);
            return pageParser.fetchPage();
        } catch (ParserException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        Crawler crawler = new Crawler("https://cse.hkust.edu.hk/~dlee/4321/", 5);
        crawler.crawl();
    }
}

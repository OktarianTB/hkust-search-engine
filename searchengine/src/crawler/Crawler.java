package crawler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.htmlparser.util.ParserException;

import utilities.PorterStemmer;
import utilities.StopWordsChecker;

public class Crawler {
    private String startUrl;
    private int maxPages;

    private PorterStemmer porter;
    private StopWordsChecker stopWords;

    private Queue<String> urlsToVisit;
    private Set<String> visitedUrls;
    
	Crawler(String startUrl, int maxPages)
	{
		this.startUrl = startUrl;
		this.maxPages = maxPages;

        porter = new PorterStemmer();
        stopWords = new StopWordsChecker();

        urlsToVisit = new LinkedList<String>();
        urlsToVisit.add(startUrl);

        visitedUrls = new HashSet<String>();
	}

    public void crawl() {
        try {
            PageParser pageParser = new PageParser(startUrl);
            Page page = pageParser.fetchPage();
            page.printPage();
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Crawler crawler = new Crawler("https://cse.hkust.edu.hk/~dlee/4321/", 30);
        crawler.crawl();
    }
}

package crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

class PageParser {
    private Parser parser;

    PageParser(String url) throws ParserException {
        parser = new Parser(url);
    }

    public Page fetchPage() throws ParserException {
        String text = getPageText();
        Vector<String> links = getPageLinks();
        String title = getPageTitle();
        int size = getPageSize(text);
        Date date = getPageDate();

        return new Page(title, text, links, size, date);
    }

    private String getPageText() throws ParserException {
        StringBean sb = new StringBean();
        sb.setLinks(false);
        parser.visitAllNodesWith(sb);
        String text = sb.getStrings();
        parser.reset();
        return text;
    }

    private Vector<String> getPageLinks() throws ParserException {
        NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
        NodeList linkList = parser.extractAllNodesThatMatch(linkFilter);
        Vector<String> links = new Vector<String>();
        LinkTag linkTag;

        for (int i = 0; i < linkList.size(); i++) {
            linkTag = (LinkTag) linkList.elementAt(i);

            try {
                URL u = new URL(linkTag.getLink());
                links.add(u.toString());
            } catch (MalformedURLException ignore) {
            }
        }

        parser.reset();
        return links;
    }

    private String getPageTitle() throws ParserException {
        String title = "";
        NodeFilter titleFilter = new NodeClassFilter(TitleTag.class);
        NodeList titleList = parser.extractAllNodesThatMatch(titleFilter);
        if (titleList.size() > 0) {
            TitleTag titleTag = (TitleTag) titleList.elementAt(0);
            if (titleTag != null) {
                title = titleTag.getTitle();
            }
        }

        parser.reset();
        return title;
    }

    public int getPageSize(String fallbackText) {
        int contentLength = parser.getConnection().getContentLength();

        if (contentLength < 0) {
            return fallbackText.length();
        }

        return contentLength;
    }

    public Date getPageDate() {
        long timestamp;
        timestamp = parser.getConnection().getLastModified();
        if (timestamp <= 0) {
            timestamp = parser.getConnection().getDate();
        }
        return new Date(timestamp);
    }

    public static void main(String[] args) throws ParserException {
        PageParser pageParser = new PageParser("https://cse.hkust.edu.hk/"); // https://cse.hkust.edu.hk/~dlee/4321/

        pageParser.fetchPage();
    }
}

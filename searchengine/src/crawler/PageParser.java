package crawler;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import utilities.LinkCleaner;

class PageParser {
    private String url;
    private Parser parser;

    PageParser(String url) throws ParserException {
        this.url = url;
        parser = new Parser(url);
    }

    public Page fetchPage() {
        try {
            String text = getPageText();
            List<String> links = getPageLinks();
            String title = getPageTitle();
            int size = getPageSize(text);
            Date lastModifiedAt = getPageLastModifiedAt();
    
            return new Page(url, title, text, links, size, lastModifiedAt);
        } catch (ParserException ignore) {
            return null;
        }
    }

    private String getPageText() throws ParserException {
        StringBean sb = new StringBean();
        sb.setLinks(false);
        parser.visitAllNodesWith(sb);
        String text = sb.getStrings();
        parser.reset();
        return text;
    }

    private List<String> getPageLinks() throws ParserException {
        NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
        NodeList linkList = parser.extractAllNodesThatMatch(linkFilter);
        List<String> links = new ArrayList<String>();
        LinkTag linkTag;

        for (int i = 0; i < linkList.size(); i++) {
            linkTag = (LinkTag) linkList.elementAt(i);

            try {
                URL u = new URL(linkTag.getLink());
                String cleanedLink = LinkCleaner.cleanLink(u.toURI());
                links.add(cleanedLink);
            } catch (MalformedURLException ignore) {
            } catch (URISyntaxException ignore) {
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

    public Date getPageLastModifiedAt() {
        long lastModifiedAt;
        lastModifiedAt = parser.getConnection().getLastModified();
        if (lastModifiedAt <= 0) {
            lastModifiedAt = parser.getConnection().getDate();
        }
        return new Date(lastModifiedAt);
    }
}

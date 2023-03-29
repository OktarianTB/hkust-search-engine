package crawler;

import java.io.UnsupportedEncodingException;
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

    // parses the page to get the title, text, links, size, and last modified date
    public Page fetchPage() {
        try {
            String title = getPageTitle();
            String text = getPageText();
            List<String> links = getPageLinks();
            int size = getPageSize();
            Date lastModifiedAt = getPageLastModifiedAt();
    
            return new Page(url, title, text, links, size, lastModifiedAt);
        } catch (ParserException ignore) {
            return null;
        } catch (UnsupportedEncodingException ignore) {
            return null;
        }
    }

    // returns the title of the page
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

    // returns the text on the page
    private String getPageText() throws ParserException {
        StringBean sb = new StringBean();
        sb.setLinks(false);
        parser.visitAllNodesWith(sb);
        String text = sb.getStrings();

        parser.reset();
        return text;
    }

    // returns a list of links on the page
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

    // returns the size of the page
    // if the page does not have a size, returns the number of characters of the raw HTML
    public int getPageSize() throws ParserException, UnsupportedEncodingException {
        int contentLength = parser.getConnection().getContentLength();

        if (contentLength < 0) {
            int size = 0;
            NodeFilter filter = new NodeClassFilter();
            NodeList nodes = parser.extractAllNodesThatMatch(filter);

            for (int i = 0; i < nodes.size(); i++) {
                size += nodes.elementAt(i).toHtml().length();
            }

            parser.reset();
            return size;
        }

        return contentLength;
    }

    // returns the last modified date of the page
    // if the page does not have a last modified date, returns the date the page was created
    public Date getPageLastModifiedAt() {
        long lastModifiedAt;
        lastModifiedAt = parser.getConnection().getLastModified();
        if (lastModifiedAt <= 0) {
            lastModifiedAt = parser.getConnection().getDate();
        }
        return new Date(lastModifiedAt);
    }
}

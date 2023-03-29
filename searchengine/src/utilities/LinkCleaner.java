package utilities;

import java.net.URI;

public class LinkCleaner {
    
    // clean the link to remove the URL fragment (the part after the # that indicates a location in the page)
    public static String cleanLink(URI uri) {
        String base = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
        
        if (uri.getQuery() == null) {
            return base;
        }
        else {
            return base + "?" + uri.getQuery();
        }
    }
}

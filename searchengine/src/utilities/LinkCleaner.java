package utilities;

import java.net.URI;

public class LinkCleaner {
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

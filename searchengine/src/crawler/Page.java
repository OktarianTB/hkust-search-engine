package crawler;

import java.util.Date;
import java.util.Vector;

class Page {
    private String title;
    private String text;
    private Vector<String> links;
    private int size;
    private Date date;

    Page(String title, String text, Vector<String> links, int size, Date date) {
        this.title = title;
        this.text = text;
        this.links = links;
        this.size = size;
        this.date = date;
    }

    public void printPage() {
        System.out.println("TITLE:\n" + title + "\n");
        System.out.println("TEXT:\n" + text + "\n");
        System.out.println("LINKS:\n");
        for (String l : links) {
            System.out.println(l);
        }
        System.out.println("\nSIZE: " + size + "\n");
        System.out.println("DATE: " + date + "\n");
    }
}

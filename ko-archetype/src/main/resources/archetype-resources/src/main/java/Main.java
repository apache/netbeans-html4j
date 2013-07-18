package ${package};

import net.java.html.boot.BrowserBuilder;

public final class Main {
    private Main() {
    }
    
    public static void main(String... args) throws Exception {
        BrowserBuilder.newBrowser().
            loadPage("index.html").
            loadClass(TwitterClient.class).
            invoke("initialize", args).
            showAndWait();
        System.exit(0);
    }
}

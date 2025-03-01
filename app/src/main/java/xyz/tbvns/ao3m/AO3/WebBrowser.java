package xyz.tbvns.ao3m.AO3;

import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;

public class WebBrowser {
    public static final WebClient client = new WebClient(BrowserVersion.CHROME){{
       getOptions().setJavaScriptEnabled(false);
       getOptions().setCssEnabled(false);
       getOptions().setDownloadImages(false);
       getOptions().setRedirectEnabled(true);
       getOptions().setTimeout(10000);
    }};

    public static void preload() {
        try {
            SearchAPI.updateAvailableParameters();
            client.getPage("https://archiveofourown.org/");
            client.getPage("https://archiveofourown.org/works");
            client.getPage("https://archiveofourown.org/media");
            client.getPage("https://archiveofourown.org/media/Anime%20*a*%20Manga/fandoms");
        } catch (Exception e) {}
    }
}

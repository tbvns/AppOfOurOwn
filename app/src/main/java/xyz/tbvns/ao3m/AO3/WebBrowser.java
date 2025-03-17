package xyz.tbvns.ao3m.AO3;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.util.Cookie;
import xyz.tbvns.ao3m.Storage.ConfigManager;

import java.io.IOException;

public class WebBrowser {
    public static final WebClient client = new WebClient(
            new BrowserVersion.BrowserVersionBuilder(BrowserVersion.BEST_SUPPORTED)
                    .setUserAgent("AppOfOurOwn (tbvns601@gmail.com)")
                    .build()
    ){{
       getOptions().setJavaScriptEnabled(false);
       getOptions().setCssEnabled(false);
       getOptions().setDownloadImages(false);
       getOptions().setRedirectEnabled(true);
       getOptions().setTimeout(20000);
    }};

    public static void preload() {
        try {
            addCookie(client, "view_adult", "true", "archiveofourown.org", "/");
            if (ConfigManager.getAccountData().getToken() != null && !ConfigManager.getAccountData().getToken().isEmpty())
                addCookie(client, "_otwarchive_session", ConfigManager.getAccountData().getToken(), "archiveofourown.org", "/");
            SearchAPI.updateAvailableParameters();
            client.getPage("https://archiveofourown.org/");
            client.getPage("https://archiveofourown.org/media");
            client.getPage("https://archiveofourown.org/media/Anime%20*a*%20Manga/fandoms");
        } catch (Exception e) {}
    }

    public static void addCookie(WebClient webClient, String name, String value, String domain, String path) {
        // Create a new Cookie object
        Cookie cookie = new Cookie(domain, name, value, path, null, false, false);

        // Add the cookie to the WebClient's CookieManager
        webClient.getCookieManager().addCookie(cookie);
    }

    @Data
    @AllArgsConstructor
    public static class Response {
        private String data;
        private boolean success;
    }

    public static Response fetch(String url) {
        try {
            return new Response(client.getPage(url).getWebResponse().getContentAsString(), true);
        } catch (IOException e) {
            return new Response(e.getMessage(), false);
        }
    }

}

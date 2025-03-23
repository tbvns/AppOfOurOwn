package xyz.tbvns.ao3m.AO3;

import androidx.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.htmlunit.BrowserVersion;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.Cookie;
import xyz.tbvns.ao3m.MainActivity;
import xyz.tbvns.ao3m.Storage.ConfigManager;
import xyz.tbvns.ao3m.Storage.Database.CacheManager;

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
        @Nullable
        private Page page;
        @Nullable
        private String message;
        private boolean success;
    }

    public static Response fetch(String url) {
        if (CacheManager.containsUrl(MainActivity.main.getApplicationContext(), url)) {
            System.out.println("Using cache for page: " + url);
            return new Response(CacheManager.get(MainActivity.main.getApplicationContext(), url), null, true);
        }

        try {
            HtmlPage page = client.getPage(url);
            CacheManager.add(MainActivity.main.getApplicationContext(), page, url);
            return new Response(page, null, true);
        } catch (Exception e) {
            return new Response(null, e.getMessage(), false);
        }
    }

}

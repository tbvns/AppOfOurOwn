package xyz.tbvns.ao3m;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import xyz.tbvns.ao3m.Storage.ConfigManager;

public class WebActivity extends AppCompatActivity {
    private WebView webView;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_web);

        // Initialize WebView
        webView = findViewById(R.id.webView);
        configureWebView();

        // Get URL from intent extras
        url = getIntent().getStringExtra("url");
        if (url == null) {
            throw new IllegalStateException("URL must be provided");
        }

        // Configure cookies and load URL
        configureCookies();
        webView.loadUrl(url);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void configureWebView() {
        System.out.println("Webview !");
        WebSettings webSettings = webView.getSettings();

        // Enable JavaScript (required for Cloudflare challenges)
        webSettings.setJavaScriptEnabled(true);

        // Set a desktop user agent to mimic a real browser
        String desktopUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        webSettings.setUserAgentString(desktopUserAgent);

        // Enable DOM storage (required for some websites)
        webSettings.setDomStorageEnabled(true);

        // Enable cookies
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        // Set WebViewClient to handle page navigation
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    private void configureCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(webView, true);
        cookieManager.setAcceptCookie(true);

        // Add adult content cookie
        addCookie("view_adult", "true", "archiveofourown.org", "/");

        // Add session cookie if available
        String sessionToken = ConfigManager.getAccountData().getToken();
        if (sessionToken != null && !sessionToken.isEmpty()) {
            addCookie("_otwarchive_session", sessionToken, "archiveofourown.org", "/");
        }
    }

    private void addCookie(String name, String value, String domain, String path) {
        String cookieString = String.format(
                "%s=%s; Domain=%s; Path=%s",
                name,
                value,
                domain,
                path
        );

        CookieManager.getInstance().setCookie(domain, cookieString);
        CookieManager.getInstance().flush();
    }
}
package xyz.tbvns.ao3m.AO3;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import lombok.extern.apachecommons.CommonsLog;
import org.htmlunit.BrowserVersion;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.Cookie;

import java.util.Set;

@CommonsLog
public class LoginAPI {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int MAX_RETRIES_FORM_LOAD = 10; // Retries for loading the form
    private static final int MAX_RETRIES_FORM_SUBMIT = 20; // Retries for submitting the form
    private static final int[] TIMEOUT_STAGES = {30000};
    public static Context appContext; // Application context for Toasts

    // Helper class to store the loaded page and its WebClient (which holds cookies)
    private static class FormData {
        HtmlPage page;
        WebClient webClient;
    }

    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
    }

    public static String login(String username, String password) {
        String loginUrl = "https://archiveofourown.org/users/login";

        new Handler(Looper.getMainLooper()).post(() -> showToast("Starting phase (1/2)."));

        // Phase 1: Load the login form with retries
        FormData formData = loadLoginForm(loginUrl);
        if (formData == null) {
            showToast("Failed to load login form after " + MAX_RETRIES_FORM_LOAD + " attempts");
            return null;
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            showToast("Phase (1/2) passed.");
            showToast("Starting phase (2/2).");
        });

        // Phase 2: Submit the login form with retries
        String authCookie = submitLoginForm(loginUrl, username, password, formData);
        if (authCookie == null) {
            showToast("Failed to submit login form after " + MAX_RETRIES_FORM_SUBMIT + " attempts");
        } else {
            new Handler(Looper.getMainLooper()).post(() -> {
                showToast("Phase (2/2) passed.");
                showToast(authCookie);
            });
        }
        // Cleanup: close the web client to free resources
        if (formData != null && formData.webClient != null) {
            formData.webClient.close();
        }
        return authCookie;
    }

    private static FormData loadLoginForm(String loginUrl) {
        for (int attempt = 0; attempt < MAX_RETRIES_FORM_LOAD; attempt++) {
            for (int timeout : TIMEOUT_STAGES) {
                WebClient webClient = new WebClient(BrowserVersion.CHROME);
                // Set options similar to our original request headers
                webClient.getOptions().setThrowExceptionOnScriptError(false);
                webClient.getOptions().setCssEnabled(false);
                webClient.getOptions().setJavaScriptEnabled(false);
                webClient.getOptions().setTimeout(timeout);
                webClient.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                webClient.addRequestHeader("Accept-Language", "en-US,en;q=0.5");
                webClient.addRequestHeader("Accept-Encoding", "gzip");
                webClient.addRequestHeader("Connection", "keep-alive");
                webClient.addRequestHeader("Referer", "https://archiveofourown.org/");
                webClient.addRequestHeader("DNT", "1");

                try {
                    HtmlPage page = webClient.getPage(loginUrl);
                    WebResponse response = page.getWebResponse();
                    int statusCode = response.getStatusCode();
                    if (statusCode == 525) {
                        showRetryToast("SSL Handshake Failed - Retrying form load...", attempt, MAX_RETRIES_FORM_LOAD);
                        webClient.close();
                        continue;
                    }
                    if (statusCode != 200) {
                        throw new RuntimeException("HTTP " + statusCode);
                    }
                    FormData formData = new FormData();
                    formData.page = page;
                    formData.webClient = webClient;
                    return formData;
                } catch (Exception e) {
                    log.error("Phase 1:" + e.getMessage());
                    String errorMessage = getErrorMessage(e);
                    showRetryToast(errorMessage + " - Retrying form load...", attempt, MAX_RETRIES_FORM_LOAD);
                    webClient.close();
                    if (attempt == MAX_RETRIES_FORM_LOAD - 1) {
                        return null;
                    }
                    try {
                        Thread.sleep(2000 * (attempt + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return null;
    }

    private static String submitLoginForm(String loginUrl, String username, String password, FormData formData) {
        for (int attempt = 0; attempt < MAX_RETRIES_FORM_SUBMIT; attempt++) {
            for (int timeout : TIMEOUT_STAGES) {
                // Adjust timeout on the WebClient
                formData.webClient.getOptions().setTimeout(timeout);
                try {
                    HtmlPage page = formData.page;
                    // Locate the form by its ID ("new_user")
                    HtmlForm form = page.getHtmlElementById("new_user");
                    if (form == null) {
                        throw new RuntimeException("Login form not found");
                    }
                    // Extract authenticity token (CSRF token)
                    HtmlInput authTokenInput = form.getInputByName("authenticity_token");
                    if (authTokenInput == null) {
                        throw new RuntimeException("CSRF token missing");
                    }
                    // Fill in username and password
                    HtmlInput loginInput = form.getInputByName("user[login]");
                    HtmlInput passwordInput = form.getInputByName("user[password]");
                    loginInput.setValueAttribute(username);
                    passwordInput.setValueAttribute(password);
                    // Set "remember me" if available
                    try {
                        HtmlInput rememberInput = form.getInputByName("user[remember_me]");
                        if (rememberInput != null) {
                            rememberInput.setValueAttribute("1");
                        }
                    } catch (Exception ignore) {}

                    // Submit the form – try to find the submit element named "commit"
                    HtmlElement submitButton = null;
                    try {
                        submitButton = form.getInputByName("commit");
                    } catch (Exception ex) {
                        // Alternatively, search for a button containing "Log in"
                        submitButton = form.getFirstByXPath(".//button[contains(text(),'Log in')]");
                    }
                    if (submitButton == null) {
                        throw new RuntimeException("Submit button not found");
                    }
                    // Clicking the submit button simulates a form submission (POST)
                    Page loginResponse = submitButton.click();
                    WebResponse response = loginResponse.getWebResponse();
                    if (response.getStatusCode() != 200) {
                        throw new RuntimeException("Login failed. Status: " + response.getStatusCode());
                    }
                    // Retrieve the session cookie from the WebClient's cookie manager
                    Set<Cookie> cookies = formData.webClient.getCookieManager().getCookies();
                    for (Cookie cookie : cookies) {
                        if ("_otwarchive_session".equals(cookie.getName())) {
                            return cookie.getValue();
                        }
                    }
                    throw new RuntimeException("Authentication cookie not found");
                } catch (Exception e) {
                    log.error("Phase 2:" + e.getMessage());
                    String errorMessage = getErrorMessage(e);
                    showRetryToast(errorMessage + " - Retrying form submit...", attempt, MAX_RETRIES_FORM_SUBMIT);
                    if (attempt == MAX_RETRIES_FORM_SUBMIT - 1) {
                        return null;
                    }
                    try {
                        Thread.sleep(2000 * (attempt + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return null;
    }

    private static String getErrorMessage(Exception e) {
        if (e.toString().contains("SSL handshake")) {
            return "SSL Error";
        } else if (e instanceof java.net.SocketTimeoutException) {
            return "Connection Timeout";
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            return "Security Error";
        }
        return "Network Error";
    }

    private static void showRetryToast(String message, int attempt, int maxRetries) {
        if (appContext != null) {
            final String toastText = message + " (Attempt " + (attempt + 1) + "/" + maxRetries + ")";
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(appContext, toastText, Toast.LENGTH_SHORT).show()
            );
        }
    }

    private static void showToast(String message) {
        if (appContext != null) {
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
            );
        }
    }
}

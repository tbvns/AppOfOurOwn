package xyz.tbvns.ao3m.Api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import lombok.extern.apachecommons.CommonsLog;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.Cookie;

import java.util.Set;

import static xyz.tbvns.ao3m.Api.WebBrowser.client;

@CommonsLog
public class LoginAPI {
    public static Context appContext;

    private static class FormData {
        HtmlPage page;
        WebClient webClient;
    }

    public static void initialize(Context context) {appContext = context.getApplicationContext();}

    public static String login(String username, String password) {
        String loginUrl = "https://archiveofourown.org/users/login";
        FormData formData = loadLoginForm(loginUrl);
        return submitLoginForm(username, password, formData);
    }

    private static FormData loadLoginForm(String loginUrl) {
        try {
            HtmlPage page = client.getPage(loginUrl);
            WebResponse response = page.getWebResponse();
            int statusCode = response.getStatusCode();
            if (statusCode == 525) {
                showToast("Cloudflare error, please try again. (HTTP 525)");
            }
            if (statusCode != 200) {
                throw new RuntimeException("HTTP " + statusCode);
            }
            FormData formData = new FormData();
            formData.page = page;
            formData.webClient = client;
            return formData;
        } catch (Exception e) {
            log.error("Phase 1:" + e.getMessage());
            String errorMessage = getErrorMessage(e);
            showToast("Error while logging in:" + errorMessage);
        }
        return null;
    }

    private static String submitLoginForm(String username, String password, FormData formData) {
        try {
            HtmlPage page = formData.page;
            HtmlForm form = page.getHtmlElementById("new_user");
            if (form == null) {
                throw new RuntimeException("Login form not found");
            }
            HtmlInput authTokenInput = form.getInputByName("authenticity_token");
            if (authTokenInput == null) {
                throw new RuntimeException("CSRF token missing");
            }
            HtmlInput loginInput = form.getInputByName("user[login]");
            HtmlInput passwordInput = form.getInputByName("user[password]");
            loginInput.setValueAttribute(username);
            passwordInput.setValueAttribute(password);
            try {
                HtmlInput rememberInput = form.getInputByName("user[remember_me]");
                if (rememberInput != null) {
                    rememberInput.setValueAttribute("1");
                }
            } catch (Exception ignore) {}

            HtmlElement submitButton = null;
            try {
                submitButton = form.getInputByName("commit");
            } catch (Exception ex) {
                submitButton = form.getFirstByXPath(".//button[contains(text(),'Log in')]");
            }
            if (submitButton == null) {
                throw new RuntimeException("Submit button not found");
            }
            Page loginResponse = submitButton.click();
            WebResponse response = loginResponse.getWebResponse();
            if (response.getStatusCode() != 200) {
                throw new RuntimeException("Login failed. Status: " + response.getStatusCode());
            }
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
            showToast("Error while submitting info: " + errorMessage);
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

    private static void showToast(String message) {
        if (appContext != null) {
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
            );
        }
    }
}

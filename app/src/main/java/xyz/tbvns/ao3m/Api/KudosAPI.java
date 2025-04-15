package xyz.tbvns.ao3m.Api;

import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSubmitInput;

import static xyz.tbvns.ao3m.Api.WebBrowser.client;

public class KudosAPI {
    public static boolean giveKudos(String pageUrl) throws Exception {
        client.getOptions().setJavaScriptEnabled(false);

        HtmlPage page = client.getPage(pageUrl);

        // Find the kudos form
        HtmlForm form = page.getHtmlElementById("new_kudo");
        if (form == null) {
            throw new RuntimeException("Kudos form not found");
        }

        // Find the submit button
        HtmlSubmitInput submitButton = form.getFirstByXPath(
                ".//input[@type='submit' and @value='Kudos ♥']");

        // Click the button to submit kudos
        HtmlPage resultPage = submitButton.click();

        // Check for error message
        DomElement kudosMessage = resultPage.getElementById("kudos_message");
        if (kudosMessage != null) {
            String className = kudosMessage.getAttribute("class");
            if (className != null && className.contains("kudos_error")) {
                return false; // Already has kudos
            }
        }

        return true; // Successfully added kudos
    }
}
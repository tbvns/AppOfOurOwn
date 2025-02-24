package xyz.tbvns.ao3m.AO3;

import lombok.SneakyThrows;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;

import java.util.ArrayList;
import java.util.List;

public class FandomAPI {
    /**
     * Uses HTMLUnit to fetch the fandom page from the given URL.
     * JavaScript is disabled here.
     *
     * @return the loaded HtmlPage
     * @throws Exception if the page cannot be retrieved
     */
    public static HtmlPage fetchFandomPage() throws Exception {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setTimeout(30000);

        HtmlPage page = webClient.getPage("https://archiveofourown.org/media");
        // Wait for background JavaScript (if needed)
        webClient.waitForBackgroundJavaScript(10000);
        return page;
    }

    /**
     * Parses the provided HtmlPage to extract a list of Category objects.
     * It finds <li> elements with classes "medium listbox group" and retrieves:
     * - The category name from the nested <h3 class="heading"><a> element.
     * - The link from the "All <category name>" button inside <p class="actions"><a>.
     *
     * @return a list of Category objects
     */
    @SneakyThrows
    public static List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        // Find all <li> elements representing a category block.
        List<HtmlElement> categoryElements = fetchFandomPage().getByXPath(
                "//li[contains(@class, 'medium') and contains(@class, 'listbox') and contains(@class, 'group')]");
        for (HtmlElement categoryElement : categoryElements) {
            // Get the category name from the <h3 class="heading"><a> element.
            HtmlAnchor categoryAnchor = categoryElement.getFirstByXPath(".//h3[contains(@class, 'heading')]/a");
            // Get the "All <category name>" link from the <p class="actions"><a> element.
            HtmlAnchor allCategoryAnchor = categoryElement.getFirstByXPath(".//p[contains(@class, 'actions')]/a");
            if (categoryAnchor != null && allCategoryAnchor != null) {
                String categoryName = categoryAnchor.asNormalizedText().trim();
                String categoryLink = allCategoryAnchor.getHrefAttribute().trim();
                categories.add(new Category(categoryName, categoryLink));
            }
        }
        return categories;
    }
}

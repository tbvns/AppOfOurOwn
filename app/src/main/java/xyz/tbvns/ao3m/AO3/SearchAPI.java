package xyz.tbvns.ao3m.AO3;

import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import xyz.tbvns.ao3m.Utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static xyz.tbvns.ao3m.AO3.WebBrowser.fetch;

public class SearchAPI {

    // Base URL for AO3 works search
    private static final String BASE_URL = "https://archiveofourown.org/works/search";

    // Cached parameters
    private static Map<String, List<Pair<String, String>>> cachedParameters = Collections.emptyMap();

    /**
     * Fetches and updates the available parameters from AO3
     */
    public static void updateAvailableParameters() {

        WebBrowser.Response response = fetch(BASE_URL);
        if (!response.isSuccess()) {
            Utils.sleep(5000);
            updateAvailableParameters();
            return; //TODO: Wow that's some shitty code (But i can't bother to fix it rn)
        }

        HtmlPage page = (HtmlPage) response.getPage();
        String html = page.asXml();
        Document document = Jsoup.parse(html);

        Map<String, List<Pair<String, String>>> newParams = new HashMap<>();
        newParams.put("languages", extractOptions(document, "select[name=work_search[language_id]]"));
        newParams.put("ratings", extractOptions(document, "select[name=work_search[rating_ids]]"));
        newParams.put("sortColumns", extractOptions(document, "select[name=work_search[sort_column]]"));
        newParams.put("sortDirections", extractOptions(document, "select[name=work_search[sort_direction]]"));

        cachedParameters = Collections.unmodifiableMap(newParams);

    }

    /**
     * Gets the cached available parameters
     */
    public static Map<String, List<Pair<String, String>>> getAvailableParameters() {
        return cachedParameters;
    }

    private static List<Pair<String, String>> extractOptions(Document document, String selector) {
        Element selectElement = document.select(selector).first();
        if (selectElement != null) {
            return selectElement.select("option").stream()
                    .map(option -> new Pair<>(option.attr("value"), option.text()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public static String generateSearchUrl(Map<String, String> params) {
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            if (queryString.length() > 0) queryString.append("&");
            queryString.append(key).append("=").append(value);
        }
        return BASE_URL + "?" + queryString.toString();
    }

    public static String getLanguageId(String language) {
        return getAvailableParameters().get("languages").stream()
                .filter(pair -> pair.second.equals(language))
                .findFirst()
                .map(pair -> pair.first)
                .orElse(null);
    }

    public static class Pair<K, V> {
        public final K first;
        public final V second;

        public Pair(K first, V second) {
            this.first = first;
            this.second = second;
        }
    }
}
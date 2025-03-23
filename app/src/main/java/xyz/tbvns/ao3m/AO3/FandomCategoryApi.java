package xyz.tbvns.ao3m.AO3;

import lombok.SneakyThrows;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static xyz.tbvns.ao3m.AO3.WebBrowser.fetch;

public class FandomCategoryApi {

    @SneakyThrows
    public static APIResponse<List<FandomCategoryObject>> getCategoryList(String url) {
        WebBrowser.Response response = fetch(url);
        if (!response.isSuccess()) {
            return new APIResponse<>(false, response.getMessage(), null);
        }
        HtmlPage page = (HtmlPage) response.getPage();
        String pageHtml = page.asXml();
        Document doc = Jsoup.parse(pageHtml);

        Elements liElements = doc.select("ul.tags.index.group li");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Callable<FandomCategoryObject>> tasks = new ArrayList<>();

        for (Element li : liElements) {
            tasks.add(() -> {
                Element aTag = li.selectFirst("a.tag");
                if (aTag != null) {
                    String name = aTag.text().trim();
                    String extraText = li.ownText().trim();
                    if (!extraText.isEmpty()) {
                        name = name + " " + extraText;
                    }
                    String link = aTag.attr("href").trim();
                    return new FandomCategoryObject(name, link);
                }
                return null;
            });
        }

        // Invoke all tasks concurrently and wait for the results.
        List<Future<FandomCategoryObject>> futures = executor.invokeAll(tasks);
        List<FandomCategoryObject> categoryList = new ArrayList<>();
        for (Future<FandomCategoryObject> future : futures) {
            FandomCategoryObject obj = future.get();
            if (obj != null) {
                categoryList.add(obj);
            }
        }
        executor.shutdown();
        return new APIResponse<>(true, null, categoryList);
    }
}

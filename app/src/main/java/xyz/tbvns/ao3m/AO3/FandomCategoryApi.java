package xyz.tbvns.ao3m.AO3;

import lombok.SneakyThrows;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FandomCategoryApi {

    @SneakyThrows
    public static List<FandomCategoryObject> getCategoryList(String url) {
        // Fetch the page using JSoup (no need for HTMLUnit)
        System.out.println("test 1: fetching page with JSoup");
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(60000)
                .get();

        System.out.println("test 3: page fetched and parsed");
        Elements liElements = doc.select("ul.tags.index.group li");
        System.out.println("test 4: found " + liElements.size() + " elements");

        // Create an ExecutorService with 5 threads
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Callable<FandomCategoryObject>> tasks = new ArrayList<>();

        // For each <li> element, create a task to extract a FandomCategoryObject.
        for (Element li : liElements) {
            tasks.add(() -> {
                Element aTag = li.selectFirst("a.tag");
                if (aTag != null) {
                    // Base category name from the <a> text.
                    String name = aTag.text().trim();
                    // Extra text (e.g., work count like "(233)") outside the <a> element.
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
        return categoryList;
    }
}

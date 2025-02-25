package xyz.tbvns.ao3m.AO3;

import lombok.SneakyThrows;
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

public class FandomAPI {
    @SneakyThrows
    public static List<FandomObject> getCategories() {
        // Fetch the page using JSoup
        Document doc = Jsoup.connect("https://archiveofourown.org/media")
                .userAgent("Mozilla/5.0")
                .timeout(60000)
                .get();

        // Select all <li> elements with classes "medium listbox group"
        Elements categoryElements = doc.select("li.medium.listbox.group");

        // Create a fixed thread pool with 5 threads
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Callable<FandomObject>> tasks = new ArrayList<>();

        // Create tasks for each category element
        for (Element li : categoryElements) {
            tasks.add(() -> {
                Element categoryAnchor = li.selectFirst("h3.heading > a");
                Element allCategoryAnchor = li.selectFirst("p.actions > a");
                if (categoryAnchor != null && allCategoryAnchor != null) {
                    String categoryName = categoryAnchor.text().trim();
                    String categoryLink = allCategoryAnchor.attr("href").trim();
                    return new FandomObject(categoryName, categoryLink);
                }
                return null;
            });
        }

        // Submit all tasks concurrently and wait for results
        List<Future<FandomObject>> futures = executor.invokeAll(tasks);
        List<FandomObject> categories = new ArrayList<>();
        for (Future<FandomObject> future : futures) {
            FandomObject obj = future.get();
            if (obj != null) {
                categories.add(obj);
            }
        }
        executor.shutdown();
        return categories;
    }
}

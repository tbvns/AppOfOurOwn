package xyz.tbvns.ao3m.AO3;

import lombok.Getter;
import lombok.SneakyThrows;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static xyz.tbvns.ao3m.AO3.WebBrowser.client;

public class ChaptersAPI {

    @Getter
    public static class Chapter {
        private final String title;
        private final String url;
        private final WorkAPI.Work work;

        public Chapter(String title, String url, WorkAPI.Work work) {
            this.title = title;
            this.url = url;
            this.work = work;
        }

        @Override
        public String toString() {
            return String.format("Chapter: %s (%s)", title, url);
        }
    }

    @SneakyThrows
    public static List<Chapter> fetchChapters(WorkAPI.Work work) {
        List<Chapter> chapters = new ArrayList<>();

        // Transform URL to navigate version
        String navigateUrl = ("https://archiveofourown.org/works/" + work.workId).replaceAll("/?$", "/navigate");
        String workUrl = ("https://archiveofourown.org/works/" + work.workId);

        // Fetch the navigate page
        HtmlPage page = client.getPage(navigateUrl);
        Document doc = Jsoup.parse(page.asXml(), workUrl);

        // Extract chapter elements
        Elements chapterLinks = doc.select("ol.chapter.index.group li a");

        for (Element link : chapterLinks) {
            String title = link.text();
            String url = link.attr("abs:href"); // Get absolute URL

            if (!title.isEmpty() && !url.isEmpty()) {
                chapters.add(new Chapter(title, url, work));
            }
        }

        return chapters;
    }

    @SneakyThrows
    public static List<String> fetchChapterParagraphs(String url) {
        List<String> paragraphs = new ArrayList<>();

        HtmlPage page = client.getPage(url);
        String pageContent = page.asXml();

        Document doc = Jsoup.parse(pageContent);

        Elements chapterParagraphs = doc.select(
                "#workskin #chapters .userstuff p"
        );

        for (Element paragraph : chapterParagraphs) {
            String text = paragraph.text();
            if (!text.isEmpty()) {
                paragraphs.add(text);
            }
        }

        System.out.println("Found paragraphs: " + paragraphs);
        System.out.println(url);
        return paragraphs;
    }
}
package xyz.tbvns.ao3m.AO3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static xyz.tbvns.ao3m.AO3.WebBrowser.client;

public class ChaptersAPI {

    @Getter
    @AllArgsConstructor
    public static class Chapter {
        private final String title;
        private final String url;
        private final String date;
        private final WorkAPI.Work work;
        private final int number;

        @Override
        public String toString() {
            return String.format("Chapter: %s (%s)", title, url);
        }
    }

    @SneakyThrows
    public static List<Chapter> fetchChapters(String workId) {
        WorkAPI.Work work = WorkAPI.fetchWork(workId);

        List<Chapter> chapters = new ArrayList<>();

        // Transform URL to navigate version
        String navigateUrl = ("https://archiveofourown.org/works/" + workId).replaceAll("/?$", "/navigate");
        String workUrl = ("https://archiveofourown.org/works/" + workId);

        // Fetch the navigate page
        HtmlPage page = client.getPage(navigateUrl);
        Document doc = Jsoup.parse(page.asXml(), workUrl);

        // Extract chapter elements
        Elements chapterLinks = doc.select("ol.chapter.index.group li a");
        Elements chapterDate = doc.select("ol.chapter.index.group li span");

        for (int i = 0; i < chapterLinks.size(); i++) {
            Element link = chapterLinks.get(i);
            Element date = chapterDate.get(i);
            String title = link.text();
            String url = link.attr("abs:href"); // Get absolute URL
            String dateString = date.text().replace("(", "").replace(")", "").replace("-", "/");

            if (!title.isEmpty() && !url.isEmpty()) {
                chapters.add(new Chapter(title, url, dateString, work, i));
            }
        }

        return chapters;
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
        Elements chapterDate = doc.select("ol.chapter.index.group li span");

        for (int i = 0; i < chapterLinks.size(); i++) {
            Element link = chapterLinks.get(i);
            Element date = chapterDate.get(i);
            String title = link.text();
            String url = link.attr("abs:href"); // Get absolute URL
            String dateString = date.text().replace("(", "").replace(")", "").replace("-", "/");

            if (!title.isEmpty() && !url.isEmpty()) {
                chapters.add(new Chapter(title, url, dateString, work, i));
            }
        }

        return chapters;
    }

    @SneakyThrows
    public static String fetchChapterParagraphs(String url) {
        HtmlPage page = client.getPage(url);
        String pageContent = page.asXml();

        Document doc = Jsoup.parse(pageContent);
        doc.select("img").remove();
        doc.select("#work.landmark.heading").remove();
        for (Element element : doc.select("blockquote")) {
            element.replaceWith(new TextNode(element.text()));
        }
        doc.select("*").removeAttr("href");

        Elements chapterParagraphs = doc.select(
                "div #chapters"
        );

        return chapterParagraphs.html();
    }
}
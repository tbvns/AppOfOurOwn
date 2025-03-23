package xyz.tbvns.ao3m.AO3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

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
    public static APIResponse<List<Chapter>> fetchChapters(String workId) {
        WorkAPI.Work work = WorkAPI.fetchWork(workId).getObject(); //TODO: This may cause error since object can be null, to fix later

        List<Chapter> chapters = new ArrayList<>();

        // Transform URL to navigate version
        String navigateUrl = ("https://archiveofourown.org/works/" + workId).replaceAll("/?$", "/navigate");
        String workUrl = ("https://archiveofourown.org/works/" + workId);

        // Fetch the navigate page
        WebBrowser.Response response = WebBrowser.fetch(navigateUrl);
        if (response.isSuccess()) {
            HtmlPage page = (HtmlPage) response.getPage();
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
        } else {
            return new APIResponse<>(false, response.getMessage(), null);
        }

        return new APIResponse<>(true, null, chapters);
    }


    @SneakyThrows
    public static APIResponse<List<Chapter>> fetchChapters(WorkAPI.Work work) {
        List<Chapter> chapters = new ArrayList<>();

        // Transform URL to navigate version
        String navigateUrl = ("https://archiveofourown.org/works/" + work.workId).replaceAll("/?$", "/navigate");
        String workUrl = ("https://archiveofourown.org/works/" + work.workId);

        // Fetch the navigate page
        WebBrowser.Response response = WebBrowser.fetch(navigateUrl);

        if (response.isSuccess()) {
            HtmlPage page = (HtmlPage) response.getPage();
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
        } else {
            return new APIResponse<>(false, response.getMessage(), null);
        }

        return new APIResponse<>(true, null, chapters);
    }

    @SneakyThrows
    public static APIResponse<String> fetchChapterParagraphs(String url) {
        WebBrowser.Response response = WebBrowser.fetch(url);
        if (response.isSuccess()) {
            HtmlPage page = (HtmlPage) response.getPage();
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

            return new APIResponse<>(true, null, chapterParagraphs.html());
        }

        return new APIResponse<>(false, response.getMessage(), null);
    }
}
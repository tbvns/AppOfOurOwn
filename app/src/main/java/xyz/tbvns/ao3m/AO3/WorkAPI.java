package xyz.tbvns.ao3m.AO3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkAPI {
    public enum ContentRating {
        general, teen, mature, explicit, none
    }

    public enum Relationship {
        ff, fm, mm, gen, multi, other, none
    }

    public enum Warning {
        unspecified, warning, none, web
    }

    public enum Status {
        incomplete, completed, none
    }

    public static class Classification {
        public ContentRating contentRating = ContentRating.none;
        public Relationship relationship = Relationship.none;
        public Warning warning = Warning.none;
        public Status status = Status.none;

        @Override
        public String toString() {
            return "Classification{" +
                    "contentRating=" + contentRating +
                    ", relationship=" + relationship +
                    ", warning=" + warning +
                    ", status=" + status +
                    '}';
        }
    }

    public static Classification classifyFanfic(String html) {
        Classification classification = new Classification();
        Document doc = Jsoup.parse(html);

        Element ratingSpan = doc.selectFirst("ul.required-tags li a span[class^=rating-]");
        if (ratingSpan != null) {
            String classes = ratingSpan.className();
            if (classes.contains("rating-general-audience")) {
                classification.contentRating = ContentRating.general;
            } else if (classes.contains("rating-teen")) {
                classification.contentRating = ContentRating.teen;
            } else if (classes.contains("rating-mature")) {
                classification.contentRating = ContentRating.mature;
            } else if (classes.contains("rating-explicit")) {
                classification.contentRating = ContentRating.explicit;
            } else if (classes.contains("rating-notrated")) {
                classification.contentRating = ContentRating.none;
            }
        }

        Element warningSpan = doc.selectFirst("ul.required-tags li a span[class^=warning-]");
        if (warningSpan != null) {
            String classes = warningSpan.className();
            if (classes.contains("warning-choosenotto")) {
                classification.warning = Warning.unspecified;
            } else if (classes.contains("warning-yes")) {
                classification.warning = Warning.warning;
            } else if (classes.contains("warning-no")) {
                classification.warning = Warning.none;
            } else if (classes.contains("warning-external-work")) {
                classification.warning = Warning.web;
            }
        }

        Element statusSpan = doc.selectFirst("ul.required-tags li a span.complete-no, ul.required-tags li a span.complete-yes");
        if (statusSpan != null) {
            String classes = statusSpan.className();
            if (classes.contains("complete-no")) {
                classification.status = Status.incomplete;
            } else if (classes.contains("complete-yes")) {
                classification.status = Status.completed;
            }
        } else {
            Element unknownStatusSpan = doc.selectFirst("ul.required-tags li a span.category-none");
            if (unknownStatusSpan != null) {
                classification.status = Status.none;
            }
        }

        Element relationshipSpan = doc.selectFirst("ul.required-tags li a span[class^=category-]");
        if (relationshipSpan != null) {
            String classes = relationshipSpan.className();
            if (classes.contains("category-femslash")) {
                classification.relationship = Relationship.ff;
            } else if (classes.contains("category-het")) {
                classification.relationship = Relationship.fm;
            } else if (classes.contains("category-gen")) {
                classification.relationship = Relationship.gen;
            } else if (classes.contains("category-slash")) {
                classification.relationship = Relationship.mm;
            } else if (classes.contains("category-multi")) {
                classification.relationship = Relationship.multi;
            } else if (classes.contains("category-other")) {
                classification.relationship = Relationship.other;
            } else if (classes.contains("category-none")) {
                classification.relationship = Relationship.none;
            }
        }

        return classification;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Work {
        public String workId;
        public String title;
        public String author;
        public String authorUrl;
        public List<String> fandoms;
        public List<String> tags;
        public Classification classification;
        public String summary;
        public String language;
        public int wordCount;
        public int chapterCount;
        public int chapterMax;
        public int hits;
        public LocalDate publishedDate;
    }

    public static List<Work> fetchWorks(String url) throws IOException {
        List<Work> works = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();

        Elements workElements = doc.select("li.work");
        for (Element workElement : workElements) {
            works.add(parseWork(workElement.outerHtml()));
        }
        return works;
    }

    public static Work parseWork(String html) {
        Document doc = Jsoup.parse(html);
        Element card = doc.selectFirst("li.work");
        if (card == null) {
            throw new IllegalArgumentException("No work card found in the provided HTML.");
        }

        // Extract the work ID (removing the "work_" prefix)
        String workId = card.id().replace("work_", "");

        // Title: the first <a> in the heading
        String title = card.selectFirst("h4.heading a").text();

        // Author and Author URL: the <a> element with rel="author"
        Element authorElement = card.selectFirst("h4.heading a[rel=author]");
        String author = authorElement != null ? authorElement.text() : "";
        String authorUrl = authorElement != null ? authorElement.attr("href") : "";

        // Fandoms: collect all fandom links in the header
        List<String> fandoms = new ArrayList<>();
        for (Element fandom : card.select("h5.fandoms a")) {
            fandoms.add(fandom.text());
        }

        // Freeform tags: from the secondary tags list
        List<String> tags = new ArrayList<>();
        for (Element tag : card.select("ul.tags.commas li a.tag")) {
            tags.add(tag.text());
        }

        // Published date: from the datetime paragraph (e.g., "26 Feb 2025")
        Element dateElement = card.selectFirst("div.header.module p.datetime");
        LocalDate publishedDate = null;
        if (dateElement != null) {
            String dateStr = dateElement.text().trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
            publishedDate = LocalDate.parse(dateStr, formatter);
        }

        // Summary: from the blockquote containing the summary
        String summary = card.selectFirst("blockquote.userstuff.summary") != null
                ? card.selectFirst("blockquote.userstuff.summary").text()
                : "";

        // Language: from the stats section
        String language = card.selectFirst("dl.stats dd.language") != null
                ? card.selectFirst("dl.stats dd.language").text().trim()
                : "";

        // Word count and hits: parsed from their respective <dd> elements
        int wordCount = parseIntFromText(card.selectFirst("dl.stats dd.words"));
        int hits = parseIntFromText(card.selectFirst("dl.stats dd.hits"));

        // Chapters: extract the current chapter count and maximum chapters (if available)
        int chapterCount = 0, chapterMax = -1;
        Element chaptersElement = card.selectFirst("dl.stats dd.chapters");
        if (chaptersElement != null) {
            String[] parts = chaptersElement.text().split("/");
            chapterCount = parseIntFromText(parts[0]);
            if (parts.length > 1 && !parts[1].trim().equals("?")) {
                chapterMax = parseIntFromText(parts[1]);
            }
        }

        // Classification: use the classifyFanfic() method on the required-tags section
        Classification classification = classifyFanfic(card.select("ul.required-tags").outerHtml());

        return new Work(workId, title, author, authorUrl, fandoms, tags, classification, summary, language, wordCount, chapterCount, chapterMax, hits, publishedDate);
    }

    private static int parseIntFromText(Element element) {
        if (element == null) return 0;
        String text = element.text().replaceAll("\\D", "");
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    private static int parseIntFromText(String text) {
        if (text == null) return 0;
        text = text.replaceAll("\\D", "");
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }
}

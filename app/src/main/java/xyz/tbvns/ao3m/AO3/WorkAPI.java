package xyz.tbvns.ao3m.AO3;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static xyz.tbvns.ao3m.AO3.WebBrowser.client;

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
        public int kudos;
        public int bookmarks;
        public LocalDate publishedDate;
    }

    @SneakyThrows
    public static List<Work> fetchWorks(String url) {
        List<Work> works = new ArrayList<>();
        HtmlPage page = client.getPage(url);
        String pageContent = page.asXml();

        // Use Jsoup to parse the HtmlUnit-rendered page
        Document doc = Jsoup.parse(pageContent);
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
        int kudos = parseIntFromText(card.selectFirst("dl.stats dd.kudos"));
        int bookmarks = parseIntFromText(card.selectFirst("dl.stats dd.bookmarks"));

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

        return new Work(workId, title, author, authorUrl, fandoms, tags, classification, summary, language, wordCount, chapterCount, chapterMax, hits, kudos, bookmarks, publishedDate);
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

    public static void applyImage(Classification c, ImageView view, int type) {
        String resourcePath = null;

        switch (type) {
            case 0: // Public (Content Rating)
                switch (c.contentRating) {
                    case general:
                        resourcePath = "icons/public/icon-general-public.png";
                        break;
                    case teen:
                        resourcePath = "icons/public/icon-teen-public.png";
                        break;
                    case mature:
                        resourcePath = "icons/public/icon-mature-public.png";
                        break;
                    case explicit:
                        resourcePath = "icons/public/icon-explicite-public.png";
                        break;
                    default:
                        resourcePath = "icons/public/icon-unknown-public.png";
                        break;
                }
                break;
            case 1: // Relationship
                switch (c.relationship) {
                    case ff:
                        resourcePath = "icons/relationship/icon-ff-relationships.png";
                        break;
                    case mm:
                        resourcePath = "icons/relationship/icon-mm-relationships.png";
                        break;
                    case fm:
                        resourcePath = "icons/relationship/icon-inter-relationships.png";
                        break;
                    case multi:
                        resourcePath = "icons/relationship/icon-multiple-relationships.png";
                        break;
                    case other:
                        resourcePath = "icons/relationship/icon-other-relationships.png";
                        break;
                    case gen:
                    case none:
                        resourcePath = "icons/relationship/icon-none-relationships.png";
                        break;
                    default:
                        resourcePath = "icons/relationship/icon-unknown-relationships.png";
                        break;
                }
                break;
            case 2: // Warnings
                switch (c.warning) {
                    case warning:
                        resourcePath = "icons/warnings/icon-has-warning.png";
                        break;
                    case unspecified:
                        resourcePath = "icons/warnings/icon-unspecified-warning.png";
                        break;
                    case web:
                        resourcePath = "icons/warnings/icon-web-warning.png";
                        break;
                    default:
                        resourcePath = "icons/warnings/icon-unknown-warning.png";
                        break;
                }
                break;
            case 3: // Status
                switch (c.status) {
                    case completed:
                        resourcePath = "icons/status/icon-done-status.png";
                        break;
                    case incomplete:
                        resourcePath = "icons/status/icon-unfinished-status.png";
                        break;
                    default:
                        resourcePath = "icons/status/icon-unknown-status.png";
                        break;
                }
                break;
        }

        if (resourcePath != null) {
            applyImageFromAssets(view, resourcePath);
        }
    }

    public static void applyImageFromAssets(ImageView view, String path) {
        try {
            Context context = view.getContext();
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(path);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            new Handler(Looper.getMainLooper()).post(() -> view.setImageBitmap(bitmap));

            inputStream.close();
        } catch (IOException e) {
            Log.e("applyImage", "Error loading image: " + path, e);
        }
    }


}

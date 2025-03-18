package xyz.tbvns.ao3m.Storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.htmlunit.Page;
import org.htmlunit.StringWebResponse;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.parser.HTMLParser;
import org.htmlunit.html.parser.neko.HtmlUnitNekoHtmlParser;
import xyz.tbvns.ao3m.AO3.WebBrowser;

import java.net.URL;

public class CacheManager {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final long CACHE_DURATION = 2 * 60 * 60 * 1000; // 2 hours in milliseconds

    public static void add(Context context, HtmlPage page, String url) throws Exception {
        // Store HTML content instead of serializing the whole page object
        String htmlContent = page.getWebResponse().getContentAsString();
        long timestamp = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put("url", url);
        values.put("html", htmlContent);
        values.put("timestamp", timestamp);

        CacheDatabaseHelper helper = new CacheDatabaseHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.insertWithOnConflict("Cache", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public static HtmlPage get(Context context, String url) {
        CacheDatabaseHelper helper = new CacheDatabaseHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT html FROM Cache WHERE url = ?",
                new String[]{url}
        )) {
            if (cursor.moveToFirst()) {
                String htmlContent = cursor.getString(0);
                return WebBrowser.client.loadHtmlCodeIntoCurrentWindow(htmlContent);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            db.close();
        }
    }

    public static boolean containsUrl(Context context, String url) {
        CacheDatabaseHelper helper = new CacheDatabaseHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM Cache WHERE url = ?",
                new String[]{url}
        )) {
            cursor.moveToFirst();
            return cursor.getInt(0) > 0;
        } finally {
            db.close();
        }
    }

    public static void clearOldCache(Context context) {
        long cutoff = System.currentTimeMillis() - CACHE_DURATION;
        CacheDatabaseHelper helper = new CacheDatabaseHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("Cache", "timestamp < ?", new String[]{String.valueOf(cutoff)});
        db.close();
    }
}
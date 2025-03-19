package xyz.tbvns.ao3m.Storage.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import xyz.tbvns.ao3m.AO3.ChaptersAPI;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HistoryEntry {
        private int workId;
        private String name;
        private long epocheDate;
        private int chapter;
        private int length;

        public ChaptersAPI.Chapter getChapterObj() {
            //TODO: This may cause error (And will cause them). To fix when the error fragment is created
            return ChaptersAPI.fetchChapters(String.valueOf(workId)).getObject().get(chapter + length);
        }
    }

    @SneakyThrows
    public static void insertWork(Context context, HistoryEntry entry) {
        HistoryDatabaseHelper dbHelper = new HistoryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 1. Check for existing entry with the same workId and chapter
        String duplicateCheckQuery =
                "SELECT 1 FROM history WHERE workId = ? AND chapter = ? LIMIT 1";
        Cursor duplicateCursor = db.rawQuery(
                duplicateCheckQuery,
                new String[]{String.valueOf(entry.getWorkId()), String.valueOf(entry.getChapter())}
        );

        boolean isDuplicate = duplicateCursor.moveToFirst();
        duplicateCursor.close();

        if (isDuplicate) {
            dbHelper.close(); // Close helper and exit
            return; // Duplicate found - do not insert
        }

        // 2. Proceed with existing logic to check for consecutive chapters
        String query = "SELECT * FROM history WHERE workId = ? ORDER BY epocheDate DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(entry.getWorkId())});

        try {
            if (cursor.moveToFirst()) {
                int lastChapter = cursor.getInt(cursor.getColumnIndexOrThrow("chapter"));
                int lastLength = cursor.getInt(cursor.getColumnIndexOrThrow("length"));
                long lastEpocheDate = cursor.getLong(cursor.getColumnIndexOrThrow("epocheDate"));
                long timeDiff = entry.getEpocheDate() - lastEpocheDate;

                System.out.println("New Chapter: " + entry.getChapter());
                System.out.println("Last Chapter + Length: " + (lastChapter + lastLength));
                System.out.println("Time Difference (s): " + timeDiff);

                // Check if chapters are consecutive and within 1 hour
                if (entry.getChapter() - 1 == lastChapter + lastLength && timeDiff <= 3600) {
                    ContentValues values = new ContentValues();
                    values.put("length", lastLength + 1);
                    db.update(
                            "history",
                            values,
                            "workId = ? AND chapter = ?",
                            new String[]{String.valueOf(entry.getWorkId()), String.valueOf(lastChapter)}
                    );
                    dbHelper.close(); // Close helper
                    return; // Merged - no new entry needed
                }
            }
        } finally {
            cursor.close();
        }

        // 3. Insert new entry if no duplicates/consecutives found
        ContentValues values = new ContentValues();
        values.put("workId", entry.getWorkId());
        values.put("name", entry.getName());
        values.put("epocheDate", entry.getEpocheDate());
        values.put("chapter", entry.getChapter());
        values.put("length", entry.getLength());
        db.insert("history", null, values);

        dbHelper.close(); // Close helper after all operations
    }
    public static List<HistoryEntry> getHistoryEntriesByWorkId(Context context, int workId) {
        return getHistoryEntries(context, "workId", String.valueOf(workId), null);
    }

    public static List<HistoryEntry> getHistoryEntriesByName(Context context, String name) {
        return getHistoryEntries(context, "name", name, null);
    }

    public static List<HistoryEntry> getHistoryEntriesByEpocheDate(Context context, long epocheDate, Long secondDate) {
        return getHistoryEntries(context, "epocheDate", String.valueOf(epocheDate),
                secondDate != null ? String.valueOf(secondDate) : String.valueOf(epocheDate + 86400));
    }

    private static List<HistoryEntry> getHistoryEntries(Context context, String column, String value, String secondValue) {
        List<HistoryEntry> entries = new ArrayList<>();
        HistoryDatabaseHelper dbHelper = new HistoryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection;
        String[] selectionArgs;

        if (column.equals("epocheDate")) {
            selection = "epocheDate BETWEEN ? AND ?";
            selectionArgs = new String[]{value, secondValue};
        } else {
            selection = column + " = ?";
            selectionArgs = new String[]{value};
        }

        Cursor cursor = db.query("history", null, selection, selectionArgs, null, null, "epocheDate DESC");
        try {
            while (cursor.moveToNext()) {
                HistoryEntry entry = new HistoryEntry();
                entry.setWorkId(cursor.getInt(cursor.getColumnIndexOrThrow("workId")));
                entry.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                entry.setEpocheDate(cursor.getLong(cursor.getColumnIndexOrThrow("epocheDate")));
                entry.setChapter(cursor.getInt(cursor.getColumnIndexOrThrow("chapter")));
                entry.setLength(cursor.getInt(cursor.getColumnIndexOrThrow("length")));
                entries.add(entry);
            }
        } finally {
            cursor.close();
            db.close();
        }
        return entries;
    }

    public static List<HistoryEntry> getHistoryEntriesPaginated(Context context, int pageNumber) {
        List<HistoryEntry> entries = new ArrayList<>();
        HistoryDatabaseHelper dbHelper = new HistoryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM history ORDER BY epocheDate DESC LIMIT ? OFFSET ?";
        int offset = pageNumber * 100;
        Cursor cursor = db.rawQuery(sql, new String[]{"100", String.valueOf(offset)});

        try {
            while (cursor.moveToNext()) {
                HistoryEntry entry = new HistoryEntry();
                entry.setWorkId(cursor.getInt(cursor.getColumnIndexOrThrow("workId")));
                entry.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                entry.setEpocheDate(cursor.getLong(cursor.getColumnIndexOrThrow("epocheDate")));
                entry.setChapter(cursor.getInt(cursor.getColumnIndexOrThrow("chapter")));
                entry.setLength(cursor.getInt(cursor.getColumnIndexOrThrow("length")));
                entries.add(entry);
            }
        } finally {
            cursor.close();
            db.close();
        }
        return entries;
    }
}
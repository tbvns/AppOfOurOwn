package xyz.tbvns.ao3m.Storage.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import xyz.tbvns.ao3m.Storage.Data.ChapterProgress;

import java.util.ArrayList;
import java.util.List;

public class ChapterProgressManager {

    public static void updateProgress(Context context, ChapterProgress progress) {
        ChapterProgressDatabaseHelper dbHelper = new ChapterProgressDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("workId", progress.getWorkId());
        values.put("chapterNumber", progress.getChapterNumber());
        values.put("progressType", progress.getProgressType());
        values.put("progress", progress.getProgress());

        db.insertWithOnConflict("chapter_progress", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        dbHelper.close();
    }

    public static List<ChapterProgress> getProgressForWork(Context context, int workId) {
        return getProgressEntries(context, "workId = ?", new String[]{String.valueOf(workId)});
    }

    public static ChapterProgress getChapterProgress(Context context, int workId, int chapterNumber) {
        ChapterProgressDatabaseHelper dbHelper = new ChapterProgressDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("chapter_progress", 
            null, 
            "workId = ? AND chapterNumber = ?",
            new String[]{String.valueOf(workId), String.valueOf(chapterNumber)}, 
            null, null, null);

        try {
            if (cursor.moveToFirst()) {
                return mapCursorToProgress(cursor);
            }
            return null;
        } finally {
            cursor.close();
            db.close();
            dbHelper.close();
        }
    }

    public static List<ChapterProgress> getAllProgress(Context context) {
        return getProgressEntries(context, null, null);
    }

    private static List<ChapterProgress> getProgressEntries(Context context, String selection, String[] selectionArgs) {
        List<ChapterProgress> entries = new ArrayList<>();
        ChapterProgressDatabaseHelper dbHelper = new ChapterProgressDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("chapter_progress", 
            null, 
            selection,
            selectionArgs, 
            null, null, "workId ASC, chapterNumber ASC");

        try {
            while (cursor.moveToNext()) {
                entries.add(mapCursorToProgress(cursor));
            }
        } finally {
            cursor.close();
            db.close();
            dbHelper.close();
        }
        return entries;
    }

    private static ChapterProgress mapCursorToProgress(Cursor cursor) {
        ChapterProgress progress = new ChapterProgress();
        progress.setWorkId(cursor.getInt(cursor.getColumnIndexOrThrow("workId")));
        progress.setChapterNumber(cursor.getInt(cursor.getColumnIndexOrThrow("chapterNumber")));
        progress.setProgressType(cursor.getString(cursor.getColumnIndexOrThrow("progressType")));
        progress.setProgress(cursor.getFloat(cursor.getColumnIndexOrThrow("progress")));
        return progress;
    }

    public static void deleteProgress(Context context, int workId, int chapterNumber) {
        ChapterProgressDatabaseHelper dbHelper = new ChapterProgressDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete("chapter_progress", 
            "workId = ? AND chapterNumber = ?",
            new String[]{String.valueOf(workId), String.valueOf(chapterNumber)});

        db.close();
        dbHelper.close();
    }

    public static boolean chapterExists(Context context, int workId, int chapterNumber) {
        ChapterProgressDatabaseHelper dbHelper = new ChapterProgressDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("chapter_progress",
                new String[]{"workId"},
                "workId = ? AND chapterNumber = ?",
                new String[]{String.valueOf(workId), String.valueOf(chapterNumber)},
                null, null, null,
                "1"); // LIMIT 1

        boolean exists = cursor.moveToFirst();

        cursor.close();
        db.close();
        dbHelper.close();

        return exists;
    }
}
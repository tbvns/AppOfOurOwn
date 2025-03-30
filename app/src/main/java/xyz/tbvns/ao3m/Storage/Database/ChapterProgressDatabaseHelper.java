package xyz.tbvns.ao3m.Storage.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChapterProgressDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "progress.db";
    private static final int DATABASE_VERSION = 1;

    public ChapterProgressDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = SQLHelper.getSQLString("InitChapterProgress.sql");
        String[] statements = sql.split(";");
        for (String statement : statements) {
            if (statement.trim().isEmpty()) continue;
            db.execSQL(statement);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implement upgrade logic if needed
    }
}
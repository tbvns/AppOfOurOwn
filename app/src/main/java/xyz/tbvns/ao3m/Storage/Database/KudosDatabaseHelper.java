package xyz.tbvns.ao3m.Storage.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KudosDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "kudos.db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    public KudosDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = SQLHelper.getSQLString("InitKudos.sql");
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
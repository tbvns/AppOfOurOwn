package xyz.tbvns.ao3m.Storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class KudosManager {
    public static boolean workExists(Context context, String workId) {
        KudosDatabaseHelper helper = new KudosDatabaseHelper(context);
        Cursor cursor = helper.getWritableDatabase().rawQuery("SELECT COUNT(*) FROM KudosWork WHERE workId = ?", new String[]{workId});
        cursor.moveToFirst();
        boolean exists = cursor.getInt(0) > 0;
        cursor.close();
        return exists;
    }

    public static void addWork(Context context, KudosWork work) {
        ContentValues values = new ContentValues();
        values.put("workId", work.workId);
        values.put("workName", work.workName);
        values.put("kudoDate", work.kudoDate);
        values.put("chapters", work.chapters);
        KudosDatabaseHelper helper = new KudosDatabaseHelper(context);
        helper.getWritableDatabase().insert("KudosWork", null, values);
    }

    public static void modifyWork(Context context, String workId, KudosWork work) {
        ContentValues values = new ContentValues();
        values.put("workId", work.workId);
        values.put("workName", work.workName);
        values.put("kudoDate", work.kudoDate);
        values.put("chapters", work.chapters);
        KudosDatabaseHelper helper = new KudosDatabaseHelper(context);
        helper.getWritableDatabase().update("KudosWork", values, "workId=?", new String[]{workId});
    }

    public static List<KudosWork> getKudosWorks(Context context, int page) {
        List<KudosWork> works = new ArrayList<>();
        int offset = page * 100;
        KudosDatabaseHelper helper = new KudosDatabaseHelper(context);
        Cursor cursor = helper.getWritableDatabase().rawQuery("SELECT workId, workName, kudoDate, chapters FROM KudosWork ORDER BY kudoDate DESC LIMIT 100 OFFSET ?", new String[]{String.valueOf(offset)});
        while (cursor.moveToNext()) {
            works.add(new KudosWork(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getLong(2),
                    cursor.getString(3)
            ));
        }
        cursor.close();
        return works;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class KudosWork {
        public String workId;
        public String workName;
        public long kudoDate;
        public String chapters;
    }
}
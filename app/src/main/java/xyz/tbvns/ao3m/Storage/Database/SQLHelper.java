package xyz.tbvns.ao3m.Storage.Database;

import lombok.SneakyThrows;
import xyz.tbvns.ao3m.Activity.MainActivity;

import java.io.InputStream;

public class SQLHelper {

    @SneakyThrows
    public static String getSQLString(String fileName) {
        InputStream is = MainActivity.main.getAssets().open("SQL/" + fileName);
        return new String(is.readAllBytes());
    }
}

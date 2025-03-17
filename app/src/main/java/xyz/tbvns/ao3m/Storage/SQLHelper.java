package xyz.tbvns.ao3m.Storage;

import lombok.SneakyThrows;
import xyz.tbvns.ao3m.MainActivity;

import java.io.InputStream;

public class SQLHelper {

    @SneakyThrows
    public static String getSQLString(String fileName) {
        InputStream is = MainActivity.main.getAssets().open("SQL/" + fileName);
        return new String(is.readAllBytes());
    }
}

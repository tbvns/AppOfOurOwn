package xyz.tbvns.ao3m.Storage;

import android.content.Context;
import lombok.SneakyThrows;
import xyz.tbvns.ao3m.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SQLHelper {

    @SneakyThrows
    public static String getSQLString(String fileName) {
        InputStream is = MainActivity.main.getAssets().open("SQL/" + fileName);
        return new String(is.readAllBytes());
    }
}

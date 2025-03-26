package xyz.tbvns.ao3m;

import android.annotation.SuppressLint;
import lombok.SneakyThrows;

public class Utils {
    @SneakyThrows
    public static void sleep(int delay) {
        Thread.sleep(delay);
    }

    @SuppressLint("DefaultLocale")
    public static String simplifyNumber(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 10_000) {
            double simplified = number / 1000.0;
            return String.format("%.1fk", simplified).replace(".0k", "k");
        } else if (number < 1_000_000) {
            double simplified = number / 1000.0;
            return String.format("%.0fk", (double) Math.round(simplified));
        } else if (number < 1_000_000_000) {
            double simplified = number / 1_000_000.0;
            return String.format("%.1fM", simplified).replace(".0M", "M");
        } else {
            double simplified = number / 1_000_000_000.0;
            return String.format("%.1fB", simplified).replace(".0B", "B");
        }
    }
}

package xyz.tbvns.ao3m.Storage.Data;

import android.graphics.Color;
import xyz.tbvns.Config;

public class CustomizationData implements Config {
    public static boolean useTextCustomSize = false;
    public static int customTextSize = 12;
    public static boolean useCustomColor = false;
    public static int textColor = Color.valueOf(1, 1, 1).toArgb();
    public static int backgroundColor = Color.valueOf(0, 0, 0).toArgb();
}

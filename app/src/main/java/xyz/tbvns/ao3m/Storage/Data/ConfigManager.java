package xyz.tbvns.ao3m.Storage.Data;

import android.content.Context;
import lombok.SneakyThrows;
import xyz.tbvns.EZConfig;

public class ConfigManager {
    @SneakyThrows
    public static void load(Context context) {
        EZConfig.setConfigFolder(context.getFilesDir().getPath());
        EZConfig.getRegisteredClasses().add(CustomizationData.class);
        EZConfig.load();
    }

    @SneakyThrows
    public static void save() {
        EZConfig.save();
    }
}

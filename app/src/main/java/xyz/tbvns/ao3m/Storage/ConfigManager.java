package xyz.tbvns.ao3m.Storage;

import android.os.Environment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import xyz.tbvns.ao3m.MainActivity;
import xyz.tbvns.ao3m.Storage.Config.LibraryConf;

import java.io.File;

public class ConfigManager {
    private static final Log log = LogFactory.getLog(ConfigManager.class);

    public static LibraryConf getLibraryConf() {
        File file = new File(MainActivity.main.getFilesDir().getPath() + "/library.json");
        if (!file.exists()) {
            LibraryConf libraryConf = new LibraryConf();
            saveLibraryConf(libraryConf);
            return libraryConf;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(file, LibraryConf.class);
        } catch (Exception e) {
            file.delete();
            return new LibraryConf();
        }
    }

    public static void saveLibraryConf(LibraryConf conf) {
        File file = new File(MainActivity.main.getFilesDir().getPath() + "/library.json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, conf);
        } catch (Exception e) {
            log.error("Failed to save Library.", e);
        }
    }
}

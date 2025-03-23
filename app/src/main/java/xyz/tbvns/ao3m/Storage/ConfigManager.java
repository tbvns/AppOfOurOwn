package xyz.tbvns.ao3m.Storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import xyz.tbvns.ao3m.MainActivity;
import xyz.tbvns.ao3m.Storage.Data.AccountData;
import xyz.tbvns.ao3m.Storage.Data.LibraryData;
import xyz.tbvns.ao3m.Storage.Data.UpdatesHistoryData;

import java.io.File;

public class ConfigManager {
    private static final Log log = LogFactory.getLog(ConfigManager.class);

    public static LibraryData getLibraryConf() {
        File file = new File(MainActivity.main.getFilesDir().getPath() + "/library.json");
        if (!file.exists()) {
            LibraryData libraryData = new LibraryData();
            saveLibraryConf(libraryData);
            return libraryData;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(file, LibraryData.class);
        } catch (Exception e) {
            file.delete();
            return new LibraryData();
        }
    }

    public static void saveLibraryConf(LibraryData conf) {
        File file = new File(MainActivity.main.getFilesDir().getPath() + "/library.json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, conf);
        } catch (Exception e) {
            log.error("Failed to save Library.", e);
        }
    }

    public static AccountData getAccountData() {
        File file = new File(MainActivity.main.getFilesDir().getPath() + "/account.json");
        if (!file.exists()) {
            AccountData accountData = new AccountData();
            saveAccountData(accountData);
            return accountData;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(file, AccountData.class);
        } catch (Exception e) {
            file.delete();
            return new AccountData();
        }
    }

    public static void saveAccountData(AccountData data) {
        File file = new File(MainActivity.main.getFilesDir().getPath() + "/account.json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (Exception e) {
            log.error("Failed to save Account data.", e);
        }
    }

    public static UpdatesHistoryData getUpdateHistoryData() {
        File file = new File(MainActivity.main.getFilesDir().getPath() + "/updateHistory.json");
        if (!file.exists()) {
            UpdatesHistoryData updatesHistoryData = new UpdatesHistoryData();
            saveUpdateHistoryData(updatesHistoryData);
            return updatesHistoryData;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(file, UpdatesHistoryData.class);
        } catch (Exception e) {
            file.delete();
            return new UpdatesHistoryData();
        }
    }

    public static void saveUpdateHistoryData(UpdatesHistoryData data) {
        File file = new File(MainActivity.main.getFilesDir().getPath() + "/updateHistory.json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (Exception e) {
            log.error("Failed to save Update History.", e);
        }
    }
}

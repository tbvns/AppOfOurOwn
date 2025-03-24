package xyz.tbvns.ao3m.Storage.Data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.tbvns.ao3m.AO3.WorkAPI;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdatesHistoryData {
    private Entry[] entries;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Entry {
        private WorkAPI.Work work;
        private long date;
        private String chapter;
        private int chapterID;
    }

    public void addEntry(Entry entry) {
        Entry[] newEntries = new Entry[entries.length + 1];
        int length = entries.length;
        if (length > 100) length = 100;

        for (int i = 0; i < length; i++) {
            newEntries[1+i] = entries[i];
        }
        newEntries[0] = entry;
        entries = newEntries;
    }

    public void removeEntry(Entry entry) {
        int count = 0;
        for (Entry e : entries) {
            if (!Objects.equals(e.chapter, entry.chapter)) {
                count++;
            }
        }
        Entry[] newEntry = new Entry[count];
        int index = 0;
        for (Entry e : entries) {
            if (!Objects.equals(e.chapter, entry.chapter)) {
                newEntry[index++] = e;
            }
        }
        entries = newEntry;
    }
}

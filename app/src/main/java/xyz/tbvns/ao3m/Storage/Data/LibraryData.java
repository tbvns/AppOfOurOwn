package xyz.tbvns.ao3m.Storage.Data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.tbvns.ao3m.Api.WorkAPI;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LibraryData {
    private WorkAPI.Work[] works = new WorkAPI.Work[0];

    public void addWork(WorkAPI.Work work) {
        WorkAPI.Work[] newWorks = new WorkAPI.Work[works.length + 1];
        for (int i = 0; i < works.length; i++) {
            newWorks[1+i] = works[i];
        }
        newWorks[0] = work;
        works = newWorks;
    }

    public void removeWork(WorkAPI.Work work) {
        int count = 0;
        for (WorkAPI.Work w : works) {
            if (!Objects.equals(w.workId, work.workId)) {
                count++;
            }
        }
        WorkAPI.Work[] newWorks = new WorkAPI.Work[count];
        int index = 0;
        for (WorkAPI.Work w : works) {
            if (!Objects.equals(w.workId, work.workId)) {
                newWorks[index++] = w;
            }
        }
        works = newWorks;
    }

    public boolean isContained(WorkAPI.Work work) {
        for (WorkAPI.Work w : works) {
            if (w.workId.equals(work.workId)) {
                return true;
            }
        }
        return false;
    }
}

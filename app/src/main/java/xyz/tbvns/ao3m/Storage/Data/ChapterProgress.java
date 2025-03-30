package xyz.tbvns.ao3m.Storage.Data;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChapterProgress {
    private int workId;
    private int chapterNumber;
    private String progressType; // "untouched", "touched", "finished"
    private float progress;
}
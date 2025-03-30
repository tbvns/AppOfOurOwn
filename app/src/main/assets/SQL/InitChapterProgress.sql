CREATE TABLE IF NOT EXISTS chapter_progress (
    workId INTEGER NOT NULL,
    chapterNumber INTEGER NOT NULL,
    progressType TEXT NOT NULL,
    progress REAL NOT NULL,
    PRIMARY KEY (workId, chapterNumber)
);

CREATE INDEX IF NOT EXISTS idx_progress_workId ON chapter_progress(workId);
CREATE INDEX IF NOT EXISTS idx_progress_chapter ON chapter_progress(chapterNumber);
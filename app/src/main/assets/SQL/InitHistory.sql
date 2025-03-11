CREATE TABLE IF NOT EXISTS history (
    workId INTEGER NOT NULL,
    name TEXT NOT NULL,
    epocheDate BIGINT NOT NULL,
    chapter INTEGER NOT NULL,
    length INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_workId ON history(workId);
CREATE INDEX IF NOT EXISTS idx_name ON history(name);
CREATE INDEX IF NOT EXISTS idx_epocheDate ON history(epocheDate);
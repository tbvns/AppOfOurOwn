CREATE TABLE IF NOT EXISTS KudosWork (
    workId TEXT NOT NULL,
    workName TEXT NOT NULL,
    kudoDate BIGINT NOT NULL,
    chapters TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_workId ON KudosWork(workId);
CREATE INDEX IF NOT EXISTS idx_workName ON KudosWork(workName);
CREATE INDEX IF NOT EXISTS idx_kudoDate ON KudosWork(kudoDate);
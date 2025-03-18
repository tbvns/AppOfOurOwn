CREATE TABLE IF NOT EXISTS Cache (
    url TEXT PRIMARY KEY,
    html TEXT NOT NULL,
    timestamp INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS cache_timestamp_idx ON Cache(timestamp);
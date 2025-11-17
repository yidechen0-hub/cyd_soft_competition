package com.cyd.cyd_soft_competition.contentdb

class scheme_sql{
    val SCHEMA_SQL = """
    CREATE TABLE IF NOT EXISTS photos (
        id TEXT PRIMARY KEY,
        path TEXT NOT NULL,
        file_size INTEGER,
        mime_type TEXT,
    
        width INTEGER,
        height INTEGER,
        orientation INTEGER,
        camera_make TEXT,
        camera_model TEXT,
    
        taken_at_utc INTEGER,
        tz_offset_min INTEGER,
        taken_at_src TEXT,
    
        latitude REAL,
        longitude REAL,
        location_accuracy_m REAL,
        location_src TEXT,
    
        file_mtime INTEGER,
        file_ctime INTEGER,
    
        caption TEXT,
        aesthetic_score REAL,
        clip_query TEXT,
        clip_vector BLOB
    );
    
    CREATE UNIQUE INDEX IF NOT EXISTS idx_photos_path ON photos(path);
    CREATE INDEX IF NOT EXISTS idx_photos_time ON photos(taken_at_utc);
    CREATE INDEX IF NOT EXISTS idx_photos_geo ON photos(latitude, longitude);
    """
}



package com.cyd.cyd_soft_competition.re_geo_code

class scheme_sql_geo {
    val SCHEMA_SQL = """
    CREATE TABLE IF NOT EXISTS geo (
        id TEXT PRIMARY KEY,
        path TEXT NOT NULL,
        
        formattedAddress TEXT,
        country TEXT,
        countryCode TEXT,
        province TEXT,
        city TEXT,
        district TEXT,
        street TEXT,
        streetNumber TEXT,
        postalCode TEXT
    );
    
    CREATE UNIQUE INDEX IF NOT EXISTS idx_photos_path ON geo(path);
    """
}
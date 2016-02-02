package de.dcja.prettygreatmusicplayer;

import android.provider.BaseColumns;

public final class MusicDataContract {
    public MusicDataContract() {}

    public static abstract class Artist implements BaseColumns {
        public static final String TABLE_NAME = "artist";
        public static final String COLUMN_NAME_ARTIST_ID = "artist_id";
        public static final String COLUMN_NAME_ARTIST_ID_FULL = TABLE_NAME + "." + COLUMN_NAME_ARTIST_ID;
        public static final String COLUMN_TYPE_ARTIST_ID = "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL";
        public static final String COLUMN_NAME_ARTIST_NAME = "artist_name";
        public static final String COLUMN_NAME_ARTIST_NAME_FULL = TABLE_NAME + "." + COLUMN_NAME_ARTIST_NAME;
        public static final String COLUMN_TYPE_ARTIST_NAME = "TEXT UNIQUE ON CONFLICT FAIL";
    }

    public static abstract class Album implements BaseColumns {
        public static final String TABLE_NAME = "album";
        public static final String COLUMN_NAME_ALBUM_ID = "album_id";
        public static final String COLUMN_NAME_ALBUM_ID_FULL = TABLE_NAME + "." + COLUMN_NAME_ALBUM_ID;
        public static final String COLUMN_TYPE_ALBUM_ID = "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL";
        public static final String COLUMN_NAME_ALBUM_NAME = "album_name";
        public static final String COLUMN_NAME_ALBUM_NAME_FULL = TABLE_NAME + "." + COLUMN_NAME_ALBUM_NAME;
        public static final String COLUMN_TYPE_ALBUM_NAME = "TEXT";
        public static final String COLUMN_NAME_ARTIST_ID = "artist_id";
        public static final String COLUMN_NAME_ARTIST_ID_FULL = TABLE_NAME + "." + COLUMN_NAME_ARTIST_ID;
        public static final String COLUMN_TYPE_ARTIST_ID = "INTEGER REFERENCES artist(artist_id) ON DELETE CASCADE";
        // It's possible that there is a more sensible option than failing on conflict, but I have yet to find out.
        public static final String CONSTRAINTS_UNIQUE =
                "UNIQUE (" + COLUMN_NAME_ARTIST_ID + ", " + COLUMN_NAME_ALBUM_NAME + ") ON CONFLICT FAIL";
    }

    public static abstract class Song implements BaseColumns {
        public static final String TABLE_NAME = "song";
        public static final String COLUMN_NAME_SONG_ID = "song_id";
        public static final String COLUMN_NAME_SONG_ID_FULL = TABLE_NAME + "." + COLUMN_NAME_SONG_ID;
        public static final String COLUMN_TYPE_SONG_ID = "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL";
        public static final String COLUMN_NAME_SONG_NAME = "song_name";
        public static final String COLUMN_NAME_SONG_NAME_FULL = TABLE_NAME + "." + COLUMN_NAME_SONG_NAME;
        public static final String COLUMN_TYPE_SONG_NAME = "TEXT";
        public static final String COLUMN_NAME_FILE_PATH = "file_path";
        public static final String COLUMN_NAME_FILE_PATH_FULL = TABLE_NAME + "." + COLUMN_NAME_FILE_PATH;
        // Replace on conflict since because of it being the same file, it can be assumed that the tags have been updated.
        public static final String COLUMN_TYPE_FILE_PATH = "TEXT UNIQUE ON CONFLICT REPLACE";
        public static final String COLUMN_NAME_ALBUM_ID = "album_id";
        public static final String COLUMN_NAME_ALBUM_ID_FULL = TABLE_NAME + "." + COLUMN_NAME_ALBUM_ID;
        public static final String COLUMN_TYPE_ALBUM_ID = "INTEGER REFERENCES album(album_id) ON DELETE CASCADE";
    }
}

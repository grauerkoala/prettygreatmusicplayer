package de.dcja.prettygreatmusicplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MusicDataDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MusicData.db";

    public static final String SQL_CREATE_ARTISTS =
            "CREATE TABLE " + MusicDataContract.Artist.TABLE_NAME + " (" +
            MusicDataContract.Artist.COLUMN_NAME_ARTIST_ID + " " + MusicDataContract.Artist.COLUMN_TYPE_ARTIST_ID + ", " +
            MusicDataContract.Artist.COLUMN_NAME_ARTIST_NAME + " " + MusicDataContract.Artist.COLUMN_TYPE_ARTIST_NAME + ")";
    public static final String SQL_CREATE_ALBUMS =
            "CREATE TABLE " + MusicDataContract.Album.TABLE_NAME + " (" +
            MusicDataContract.Album.COLUMN_NAME_ALBUM_ID + " " + MusicDataContract.Album.COLUMN_TYPE_ALBUM_ID + ", " +
            MusicDataContract.Album.COLUMN_NAME_ALBUM_NAME + " " + MusicDataContract.Album.COLUMN_TYPE_ALBUM_NAME + ", " +
            MusicDataContract.Album.COLUMN_NAME_ARTIST_ID + " " + MusicDataContract.Album.COLUMN_TYPE_ARTIST_ID + ", " +
            MusicDataContract.Album.CONSTRAINTS_UNIQUE + ")";
    public static final String SQL_CREATE_SONGS =
            "CREATE TABLE " + MusicDataContract.Song.TABLE_NAME + " (" +
            MusicDataContract.Song.COLUMN_NAME_SONG_ID + " " + MusicDataContract.Song.COLUMN_TYPE_SONG_ID + ", " +
            MusicDataContract.Song.COLUMN_NAME_SONG_NAME + " " + MusicDataContract.Song.COLUMN_TYPE_SONG_NAME + ", " +
            MusicDataContract.Song.COLUMN_NAME_FILE_PATH + " " + MusicDataContract.Song.COLUMN_TYPE_FILE_PATH + ", " +
            MusicDataContract.Song.COLUMN_NAME_ALBUM_ID + " " + MusicDataContract.Song.COLUMN_TYPE_ALBUM_ID + ")";

    public static final String SQL_DELETE_SONGS = "DROP TABLE IF EXISTS " + MusicDataContract.Song.TABLE_NAME;
    public static final String SQL_DELETE_ALBUMS = "DROP TABLE IF EXISTS " + MusicDataContract.Album.TABLE_NAME;
    public static final String SQL_DELETE_ARTISTS = "DROP TABLE IF EXISTS " + MusicDataContract.Artist.TABLE_NAME;

    public MusicDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ARTISTS);
        db.execSQL(SQL_CREATE_ALBUMS);
        db.execSQL(SQL_CREATE_SONGS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Since the database doesn't contain any data that isn't available elsewhere (file tags),
        // it's perfectly fine(-ish) to just start over.
        db.execSQL(SQL_DELETE_SONGS);
        db.execSQL(SQL_DELETE_ALBUMS);
        db.execSQL(SQL_DELETE_ARTISTS);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

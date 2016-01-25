package de.dcja.prettygreatmusicplayer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;

/**
 * Grabs information about songs from either the database or the file.
 */
public class MusicInfoRetriever {

    private MediaMetadataRetriever mediaMetadataRetriever;
    private MusicDataDbHelper musicDataDbHelper;
    private SQLiteDatabase db;

    private String artist;
    private String album;
    private String song;

    // If file is not yet present in database, fall back to reading the data from the file
    private boolean fallbackToFile;

    private static final String RAW_QUERY_DATA_BY_FILE = "SELECT " +
            MusicDataContract.Artist.TABLE_NAME + "." + MusicDataContract.Artist.COLUMN_NAME_ARTIST_NAME + " artist, " +
            MusicDataContract.Album.TABLE_NAME + "." + MusicDataContract.Album.COLUMN_NAME_ALBUM_NAME + " album, " +
            MusicDataContract.Song.TABLE_NAME + "." + MusicDataContract.Song.COLUMN_NAME_SONG_NAME + " song" +
            " FROM " + MusicDataContract.Artist.TABLE_NAME + ", " +
            MusicDataContract.Album.TABLE_NAME + ", " +
            MusicDataContract.Song.TABLE_NAME +
            " WHERE " + MusicDataContract.Song.TABLE_NAME + "." + MusicDataContract.Song.COLUMN_NAME_FILE_PATH + " = ?" +
            " AND " + MusicDataContract.Song.TABLE_NAME + "." + MusicDataContract.Song.COLUMN_NAME_ALBUM_ID + " = " +
            MusicDataContract.Album.TABLE_NAME + "." + MusicDataContract.Album.COLUMN_NAME_ALBUM_ID +
            " AND " + MusicDataContract.Album.TABLE_NAME + "." + MusicDataContract.Album.COLUMN_NAME_ARTIST_ID + " = " +
            MusicDataContract.Artist.TABLE_NAME + "." + MusicDataContract.Artist.COLUMN_NAME_ARTIST_ID;

    public MusicInfoRetriever(Context context, boolean fallbackToFile) {
        musicDataDbHelper = new MusicDataDbHelper(context);
        db = musicDataDbHelper.getReadableDatabase();

        artist = null;
        album = null;
        song = null;

        this.fallbackToFile = fallbackToFile;
    }

    public void setDataSource(String fileName) {
        artist = null;
        album = null;
        song = null;

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(RAW_QUERY_DATA_BY_FILE, new String[]{fileName});
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"));
                album = cursor.getString(cursor.getColumnIndexOrThrow("album"));
                song = cursor.getString(cursor.getColumnIndexOrThrow("song"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (artist == null && fallbackToFile) {
            if (mediaMetadataRetriever == null) {
                mediaMetadataRetriever = new MediaMetadataRetriever();
            }
            mediaMetadataRetriever.setDataSource(fileName);
            artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            album = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            song = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        }
    }

    public String getArtist() {
        return artist == null ? "" : artist;
    }

    public String getAlbum() {
        return album == null ? "" : album;
    }

    public String getSong() {
        return song == null ? "" : song;
    }
}

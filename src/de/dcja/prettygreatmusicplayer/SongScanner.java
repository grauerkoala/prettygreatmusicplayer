package de.dcja.prettygreatmusicplayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SongScanner {
    private final Activity activity;
    private final File path;
    private final ProgressDialog pd;
    private final Handler handler;
    private MusicDataDbHelper musicDataDbHelper;
    private boolean canceled = false;

    private final String TAG = "SongScanner";

    public static void rescanMusic(Activity activity, File path) {
        if (path == null || !path.isDirectory()) {
            return;
        }
        final SongScanner songScanner = new SongScanner(activity, path);
        // Handle progress in separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                songScanner.scan();
            }
        }).start();
    }

    private SongScanner(final Activity activity, final File path) {
        this.activity = activity;
        this.path = path;
        handler = new Handler();
        pd = new ProgressDialog(activity);
        musicDataDbHelper = new MusicDataDbHelper(activity);

        pd.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pd.dismiss();
                canceled = true;
            }
        });
    }

    private void scan() {
        showRescanDialog();

        // List ALL the files recursively
        // https://stackoverflow.com/a/10814316/3625721
        final List<File> files = new ArrayList<File>();
        Queue<File> dirs = new LinkedList<File>();
        dirs.add(path);
        while (!dirs.isEmpty()) {
            for (File f : dirs.poll().listFiles()) {
                if (f.isDirectory()) {
                    dirs.add(f);
                } else if (f.isFile() && Utils.isValidSongFile(f)) {
                    files.add(f);
                }
            }
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                pd.setMax(files.size());
                pd.setIndeterminate(false);
            }
        });

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

        for (int i = 0; i < files.size(); ++i) {
            if (canceled) {
                break;
            }

            final File file = files.get(i);
            final int progress = i;
            final String fileName = file.getName();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    pd.setProgress(progress);
                    pd.setMessage(fileName);
                }
            });

            mediaMetadataRetriever.setDataSource(file.getPath());
            String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String songTitle = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

            if (artist == null) {
                artist = "Unknown";
            }

            if (album == null) {
                album = "Unknown";
            }

            if (songTitle == null) {
                songTitle = file.getName();
                songTitle = songTitle.substring(0, songTitle.lastIndexOf('.'));
            }

            addSongToDb(file.getAbsolutePath(), songTitle, album, artist);
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                pd.dismiss();
            }
        });
    }

    private void showRescanDialog() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setTitle(activity.getString(R.string.rescan_dialog_title));
                pd.setMessage(activity.getString(R.string.rescan_dialog_counting));
                pd.setIndeterminate(true);
                pd.setCancelable(false);
                pd.setProgress(0);
                pd.setMax(1);
                pd.show();
            }
        });
    }

    private void addSongToDb(final String filePath,
                             final String songTitle, final String album, final String artist) {
        SQLiteDatabase db = musicDataDbHelper.getWritableDatabase();
        Cursor cursor;

        Log.i(TAG, "Handling: " + artist + " - " + album + " - " + songTitle + " (" + filePath + ")");

        // Retrieve artist_id; add artist if not yet present
        long artist_id = -1;
        cursor = db.query(MusicDataContract.Artist.TABLE_NAME,
                new String[]{MusicDataContract.Artist.COLUMN_NAME_ARTIST_ID},
                MusicDataContract.Artist.COLUMN_NAME_ARTIST_NAME + " = ?",
                new String[]{artist},
                null, null, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            artist_id = cursor.getLong(cursor.getColumnIndexOrThrow(
                    MusicDataContract.Artist.COLUMN_NAME_ARTIST_ID));
            Log.i(TAG, "Artist \"" + artist + "\" was present with ID: " + artist_id);
        } else if (cursor.getCount() == 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicDataContract.Artist.COLUMN_NAME_ARTIST_NAME, artist);
            artist_id = db.insert(MusicDataContract.Artist.TABLE_NAME, null, contentValues);
            Log.i(TAG, "Artist \"" + artist + "\" was added. New ID: " + artist_id);
        } else {
            // This shouldn't happen, because it would mean there are two artists of the same name
            // even though the column was given the UNIQUE constraint. But just in case...
            Log.e(TAG, "More than one artist \"" + artist + "\"!");
            return;
        }
        cursor.close();

        // Retrieve album_id; add album if not yet present
        long album_id = -1;
        cursor = db.query(MusicDataContract.Album.TABLE_NAME,
                new String[]{MusicDataContract.Album.COLUMN_NAME_ALBUM_ID},
                MusicDataContract.Album.COLUMN_NAME_ALBUM_NAME + " = ? AND " +
                MusicDataContract.Album.COLUMN_NAME_ARTIST_ID + " = ?",
                new String[]{album, Long.toString(artist_id)},
                null, null, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            album_id = cursor.getLong(cursor.getColumnIndexOrThrow(
                    MusicDataContract.Album.COLUMN_NAME_ALBUM_ID));
            Log.i(TAG, "Album \"" + album + "\" was present with ID: " + album_id);
        } else if (cursor.getCount() == 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicDataContract.Album.COLUMN_NAME_ALBUM_NAME, album);
            contentValues.put(MusicDataContract.Album.COLUMN_NAME_ARTIST_ID, artist_id);
            artist_id = db.insert(MusicDataContract.Album.TABLE_NAME, null, contentValues);
            Log.i(TAG, "Album \"" + album + "\" was added. New ID: " + album_id);
        } else {
            // This shouldn't happen
            Log.e(TAG, "More than one album \"" + album + "\" by artist with ID " + artist_id + "!");
            return;
        }
        cursor.close();

        // If file is present already, update it. Otherwise add it
        long song_id = -1;
        cursor = db.query(MusicDataContract.Song.TABLE_NAME,
                new String[]{MusicDataContract.Song.COLUMN_NAME_SONG_ID},
                MusicDataContract.Song.COLUMN_NAME_FILE_PATH + " = ?",
                new String[]{filePath},
                null, null, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            song_id = cursor.getLong(cursor.getColumnIndexOrThrow(
                    MusicDataContract.Song.COLUMN_NAME_SONG_ID));
            Log.i(TAG, "File \"" + filePath + "\" was present with ID: " + song_id);
            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicDataContract.Song.COLUMN_NAME_SONG_NAME, songTitle);
            contentValues.put(MusicDataContract.Song.COLUMN_NAME_ALBUM_ID, album_id);
            db.update(MusicDataContract.Song.TABLE_NAME,
                    contentValues,
                    MusicDataContract.Song.COLUMN_NAME_SONG_ID + " = ?",
                    new String[]{Long.toString(song_id)});
            Log.i(TAG, "Song with ID " + song_id + " was updated to be \"" + songTitle + "\" with album ID: " + album_id);
        } else if (cursor.getCount() == 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicDataContract.Song.COLUMN_NAME_SONG_NAME, songTitle);
            contentValues.put(MusicDataContract.Song.COLUMN_NAME_ALBUM_ID, album_id);
            contentValues.put(MusicDataContract.Song.COLUMN_NAME_FILE_PATH, filePath);
            song_id = db.insert(MusicDataContract.Song.TABLE_NAME, null, contentValues);
            Log.i(TAG, "File " + filePath + " was added. New ID: " + song_id);
        } else {
            // This shouldn't happen
            Log.e(TAG, "More than one file \"" + filePath + "\"!");
            return;
        }
        cursor.close();
    }
}

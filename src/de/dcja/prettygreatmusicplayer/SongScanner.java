package de.dcja.prettygreatmusicplayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.os.Handler;

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
    private boolean canceled = false;

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
            String songTitle = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
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
}

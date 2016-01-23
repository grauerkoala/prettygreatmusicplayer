package de.dcja.prettygreatmusicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class PlayerControlWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "PlayerControlWidget";

    public static final String ACTION_PLAYBACK_STATUS_CHANGED = "de.dcja.prettygreatmusicplayer.PLAYBACK_STATUS_CHANGED";

    private Bundle playbackInfo;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch NowPlaying
            Intent intent = new Intent(context, NowPlaying.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_player_control);
            if (playbackInfo != null) {
                views.setOnClickPendingIntent(R.id.playButton, pendingIntent);
                views.setTextViewText(R.id.songTitle, playbackInfo.getString(MusicPlaybackService.PRETTY_SONG_NAME));
                views.setTextViewText(R.id.songArtist, playbackInfo.getString(MusicPlaybackService.PRETTY_ARTIST_NAME));
                views.setTextViewText(R.id.songAlbum, playbackInfo.getString(MusicPlaybackService.PRETTY_ALBUM_NAME));
                views.setProgressBar(R.id.songProgress,
                        playbackInfo.getInt(MusicPlaybackService.TRACK_DURATION),
                        playbackInfo.getInt(MusicPlaybackService.TRACK_POSITION), false);
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_PLAYBACK_STATUS_CHANGED)) {
            Bundle b = intent.getBundleExtra("updateInfo");
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_player_control);
            // Doesn't work for some reason
            // views.setTextViewText(R.id.songTitle, b.getString(MusicPlaybackService.PRETTY_SONG_NAME));
            playbackInfo = b;

            // Update widgets
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, PlayerControlWidgetProvider.class));
            this.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }
}

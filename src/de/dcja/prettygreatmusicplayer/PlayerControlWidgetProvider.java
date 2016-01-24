package de.dcja.prettygreatmusicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_player_control);
            if (playbackInfo != null && playbackInfo.getInt(MusicPlaybackService.TRACK_DURATION) > 0) {
                String songName = playbackInfo.getString(MusicPlaybackService.PRETTY_SONG_NAME);
                String artistName = playbackInfo.getString(MusicPlaybackService.PRETTY_ARTIST_NAME);
                String albumName = playbackInfo.getString(MusicPlaybackService.PRETTY_ALBUM_NAME);
                int trackDuration = playbackInfo.getInt(MusicPlaybackService.TRACK_DURATION);
                int trackPosition = playbackInfo.getInt(MusicPlaybackService.TRACK_POSITION);
                views.setTextViewText(R.id.songTitle, songName);
                views.setTextViewText(R.id.songArtist, artistName);
                views.setTextViewText(R.id.songAlbum, albumName);
                setWidgetMode(context, views, true);
                views.setProgressBar(R.id.songProgress, trackDuration, trackPosition, false);
            } else {
                setWidgetMode(context, views, false);
            }

            views.setOnClickPendingIntent(R.id.widget_inactive_view, pendingIntent);

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
            // For some reason, manipulating views here doesn't work, so we'll have to save the bundle
            playbackInfo = b;

            // Update widgets
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, PlayerControlWidgetProvider.class));
            this.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    private void setWidgetMode(Context context, RemoteViews views, boolean active) {
        for (int view : new int[]{R.id.songProgress, R.id.widget_active_view}) {
            views.setViewVisibility(view, active ? View.VISIBLE : View.GONE);
        }
        views.setViewVisibility(R.id.widget_inactive_view, active ? View.GONE : View.VISIBLE);
    }
}

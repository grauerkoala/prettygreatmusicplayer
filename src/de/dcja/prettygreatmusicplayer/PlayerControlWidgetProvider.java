package de.dcja.prettygreatmusicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

public class PlayerControlWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "PlayerControlWidget";

    public static final String ACTION_PLAYBACK_STATUS_CHANGED = "de.dcja.prettygreatmusicplayer.PLAYBACK_STATUS_CHANGED";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        PlaybackInfoHolder pih = PlaybackInfoHolder.getInstance();
        Playlist playlist;
        Playlist.Song song = null;
        int trackDuration = 0;
        int trackPosition = 0;
        synchronized (pih) {
            playlist = pih.getActivePlaylist();
            if (playlist != null) {
                song = playlist.getPlaying();
                trackDuration = pih.getTrackDuration();
                trackPosition = pih.getTrackPosition();
            }
        }
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch NowPlaying
            Intent intent = new Intent(context, NowPlaying.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_player_control);
            if (playlist != null && song != null) {
                views.setTextViewText(R.id.songTitle, song.getSong());
                views.setTextViewText(R.id.songArtist, song.getArtist());
                views.setTextViewText(R.id.songAlbum, song.getAlbum());
                setWidgetMode(views, true);
                views.setProgressBar(R.id.songProgress, trackDuration, trackPosition, false);
            } else {
                setWidgetMode(views, false);
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
            // Update widgets
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, PlayerControlWidgetProvider.class));
            this.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    private void setWidgetMode(RemoteViews views, boolean active) {
        for (int view : new int[]{R.id.songProgress, R.id.widget_active_view}) {
            views.setViewVisibility(view, active ? View.VISIBLE : View.GONE);
        }
        views.setViewVisibility(R.id.widget_inactive_view, active ? View.GONE : View.VISIBLE);
    }
}

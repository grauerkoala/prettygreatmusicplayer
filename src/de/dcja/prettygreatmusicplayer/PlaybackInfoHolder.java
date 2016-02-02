package de.dcja.prettygreatmusicplayer;

/**
 * This is a singleton class to share playback information across the whole application from a
 * single spot rather than spamming broadcasts with all pieces of information.
 *
 * This singleton is going to be used by several threads simultaneously, therefore synchronization
 * is required. When working with the playlist encapsulated in this class, synchronization should
 * happen based on the <code>PlaybackInfoHolder</code> instance.
 */
public class PlaybackInfoHolder {
    private static PlaybackInfoHolder instance;

    private Playlist activePlaylist;
    private MusicPlaybackService.PlaybackState playbackState;
    private int trackDuration;
    private int trackPosition;

    private PlaybackInfoHolder() {
        activePlaylist = null;
        playbackState = null;
    }

    public static synchronized PlaybackInfoHolder getInstance() {
        if (instance == null) {
            instance = new PlaybackInfoHolder();
        }

        return instance;
    }

    public synchronized Playlist getActivePlaylist() {
        return activePlaylist;
    }

    public synchronized void setActivePlaylist(Playlist activePlaylist) {
        this.activePlaylist = activePlaylist;
    }

    public synchronized MusicPlaybackService.PlaybackState getPlaybackState() {
        return playbackState;
    }

    public synchronized void setPlaybackState(MusicPlaybackService.PlaybackState playbackState) {
        this.playbackState = playbackState;
    }

    public synchronized int getTrackDuration() {
        return trackDuration;
    }

    public synchronized void setTrackDuration(int trackDuration) {
        this.trackDuration = trackDuration;
    }

    public synchronized int getTrackPosition() {
        return trackPosition;
    }

    public synchronized void setTrackPosition(int trackPosition) {
        this.trackPosition = trackPosition;
    }
}

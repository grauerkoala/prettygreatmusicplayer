package de.dcja.prettygreatmusicplayer;

/**
 * This is a singleton class to share playback information across the whole application from a
 * single spot rather than spamming broadcasts with all pieces of information.
 */
public class PlaybackInfoHolder {
    private static PlaybackInfoHolder instance;

    private Playlist activePlaylist;
    private MusicPlaybackService.PlaybackState playbackState;

    private PlaybackInfoHolder() {
        activePlaylist = null;
        playbackState = null;
    }

    public PlaybackInfoHolder getInstance() {
        if (instance == null) {
            instance = new PlaybackInfoHolder();
        }

        return instance;
    }

    public Playlist getActivePlaylist() {
        return activePlaylist;
    }

    public void setActivePlaylist(Playlist activePlaylist) {
        this.activePlaylist = activePlaylist;
    }

    public MusicPlaybackService.PlaybackState getPlaybackState() {
        return playbackState;
    }

    public void setPlaybackState(MusicPlaybackService.PlaybackState playbackState) {
        this.playbackState = playbackState;
    }
}

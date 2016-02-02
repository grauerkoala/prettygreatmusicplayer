package de.dcja.prettygreatmusicplayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Handles composition and navigation of a playlist.
 */
public class Playlist {
    private ArrayList<PlaylistItem> playlist;
    private ArrayList<PlaylistItem> playorder; // when in shuffle mode

    private PlaylistItem playing;
    private int position; // refers to either playlist or playorder, depending on shuffle mode

    private boolean shuffling;


    public Playlist() {
        this(null);
    }

    public Playlist(List<Song> songs) {
        playlist = new ArrayList<>();
        playorder = new ArrayList<>();

        if (songs != null) {
            appendSongs(songs);
        }

        shuffling = false;

        position = -1;
    }

    public void insertSong(int index, Song song) {
        List<Song> songs = new LinkedList<Song>();
        songs.add(song);
        insertSongs(index, songs);
    }

    public void insertSongs(int index, List<Song> songs) {
        ArrayList<PlaylistItem> items = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            items.add(new PlaylistItem(songs.get(i), index + i));
        }

        // Shift existing items after insertion point up by the necessary amount of spots
        for (int i = index; i < playlist.size(); i++) {
            playlist.get(i).position += songs.size();
        }

        playlist.addAll(index, items);

        addItemsToPlayorder(items);
    }

    public void appendSong(Song song) {
        insertSong(playlist.size(), song);
    }

    public void appendSongs(List<Song> songs) {
        insertSongs(playlist.size(), songs);
    }

    /**
     * If the playlist is in shuffle mode, the given songs are added at random positions within the
     * playorder, otherwise they are simply appended since playorder gets shuffled every time the
     * playlist is put into shuffle mode.
     */
    private void addItemsToPlayorder(Collection<PlaylistItem> items) {
        if (shuffling) {
            Random random = new Random(System.currentTimeMillis());
            for (PlaylistItem item : items) {
                playorder.add(random.nextInt(playorder.size()), item);
            }
        } else {
            playorder.addAll(items);
        }
    }

    /**
     * Moves to the next song and returns its position. Currently assumes that playlists are always
     * repeated.
     * @return The position of the next song.
     */
    public int next() {
        if (playlist.size() == 0) {
            return -1;
        }
        position = (position + 1) % playlist.size();
        playing = shuffling ? playorder.get(position) : playlist.get(position);
        return playing.position;
    }

    public int peekNext() {
        if (playlist.size() == 0) {
            return -1;
        }
        int pos = (position + 1) % playlist.size();
        return (shuffling ? playorder : playlist).get(pos).position;
    }

    public int previous() {
        if (playlist.size() == 0) {
            return -1;
        }
        position = (position - 1) % playlist.size();
        playing = shuffling ? playorder.get(position) : playlist.get(position);
        return playing.position;
    }

    public int peekPrevious() {
        if (playlist.size() == 0) {
            return -1;
        }
        if (position < 0) {
            position = 0;
        }
        int pos = (position - 1) % playlist.size();
        return (shuffling ? playorder : playlist).get(pos).position;
    }

    /**
     * Replaces the currently playing song with the song at the given position. The position refers
     * to the position of the song in the playlist rather than the playorder.
     */
    public void selectSong(int position) {
        playing = playlist.get(position);
        this.position = shuffling ? playorder.indexOf(playing) : playing.position;
    }

    /**
     * Returns the position of the currently playing song in the playlist or -1 if there is no such
     * song.
     *
     * If the playlist is in shuffle mode, this position still refers to the original position of
     * the song.
     * @return The position of the currently playing song in the playlist or -1 if there is no such
     * song.
     */
    public int getPosition() {
        return playing != null ? playing.position : -1;
    }

    public List<Song> getPlaylist() {
        List<Song> songs = new ArrayList<>();
        for (PlaylistItem item : playlist) {
            songs.add(item.song);
        }
        return songs;
    }

    public Song getPlaying() {
        return playing.song;
    }

    public int getCount() {
        return playlist.size();
    }

    public boolean isShuffling() {
        return shuffling;
    }

    public void setShuffling(boolean shuffling) {
        this.shuffling = shuffling;
        if (!shuffling) {
            position = getPosition();
        } else {
            Collections.shuffle(playorder);
            position = playorder.indexOf(playing);
        }
    }

    /**
     * Contains a path to a song file and associated metadata
     */
    public class Song {
        private final String fileName;
        private final String artist;
        private final String album;
        private final String song;

        public Song(final String fileName, final String artist, final String album, final String song) {
            this.fileName = fileName;
            this.artist = artist;
            this.album = album;
            this.song = song;
        }

        public String getFileName() {
            return fileName;
        }

        public String getArtist() {
            return artist;
        }

        public String getAlbum() {
            return album;
        }

        public String getSong() {
            return song;
        }
    }

    /**
     * Contains a song and an index of the item in the playlist
     */
    private class PlaylistItem {
        Song song;
        int position;

        public PlaylistItem(Song song, int position) {
            this.song = song;
        }
    }
}


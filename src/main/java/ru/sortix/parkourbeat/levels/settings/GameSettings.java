package ru.sortix.parkourbeat.levels.settings;

public class GameSettings {

    protected final String songPlayListName, songName;

    public GameSettings(String songPlayListName, String songName) {
        this.songPlayListName = songPlayListName;
        this.songName = songName;
    }

    public String getSongPlayListName() {
        return songPlayListName;
    }

    public String getSongName() {
        return songName;
    }

}
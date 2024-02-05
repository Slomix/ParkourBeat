package ru.sortix.parkourbeat.levels.settings;

public class GameSettings {

    protected final String songPlayListName, songName, regionName;

    public GameSettings(String songPlayListName, String songName, String regionName) {
        this.songPlayListName = songPlayListName;
        this.songName = songName;
        this.regionName = regionName;
    }

    public String getSongPlayListName() {
        return songPlayListName;
    }

    public String getSongName() {
        return songName;
    }

    public String getRegionName() {
        return regionName;
    }
}
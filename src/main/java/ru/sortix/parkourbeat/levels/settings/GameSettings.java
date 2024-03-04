package ru.sortix.parkourbeat.levels.settings;

import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class GameSettings {
    private final UUID levelId;
    private final String levelName;
    private final String ownerName;
    private String songPlayListName;
    private String songName;

    public GameSettings(
            @NonNull UUID levelId,
            @NonNull String levelName,
            String songPlayListName,
            String songName,
            String ownerName) {
        this.levelId = levelId;
        this.levelName = levelName;
        this.songPlayListName = songPlayListName;
        this.songName = songName;
        this.ownerName = ownerName;
    }

    public void setSong(String playlist, String name) {
        this.songPlayListName = playlist;
        this.songName = name;
    }
}

package ru.sortix.parkourbeat.levels.dao.files;

import java.util.UUID;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

public class GameSettingsDAO {

    public void set(GameSettings gameSettings, FileConfiguration config) {
        config.set("level_name", gameSettings.getLevelName());
        config.set("song_play_list_name", gameSettings.getSongPlayListName());
        config.set("song_name", gameSettings.getSongName());
        config.set("owner", gameSettings.getOwnerName());
    }

    public GameSettings load(@NonNull UUID levelId, FileConfiguration config) {
        String levelName = config.getString("level_name");
        if (levelName == null) {
            throw new IllegalArgumentException("level_name not found");
        }
        String songPlayListName = config.getString("song_play_list_name");
        String songName = config.getString("song_name");
        String owner = config.getString("owner");
        return new GameSettings(levelId, levelName, songPlayListName, songName, owner);
    }
}

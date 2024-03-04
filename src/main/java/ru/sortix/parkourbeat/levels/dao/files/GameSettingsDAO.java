package ru.sortix.parkourbeat.levels.dao.files;

import java.util.UUID;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

@RequiredArgsConstructor
public class GameSettingsDAO {
    private final Logger logger;

    public void set(GameSettings gameSettings, FileConfiguration config) {
        config.set("level_name", gameSettings.getLevelName());
        config.set("song_play_list_name", gameSettings.getSongPlayListName());
        config.set("song_name", gameSettings.getSongName());
        config.set("owner_id", gameSettings.getOwnerId().toString());
        config.set("owner_name", gameSettings.getOwnerName());
    }

    public GameSettings load(@NonNull UUID levelId, FileConfiguration config) {
        String levelName = config.getString("level_name");
        if (levelName == null) {
            throw new IllegalArgumentException("level_name not found");
        }
        String songPlayListName = config.getString("song_play_list_name");
        String songName = config.getString("song_name");
        UUID ownerId = UUID.fromString(config.getString("owner_id", null));
        String ownerName = config.getString("owner_name");
        return new GameSettings(levelId, levelName, ownerId, ownerName, songPlayListName, songName);
    }
}

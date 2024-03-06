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
        config.set("owner_id", gameSettings.getOwnerId().toString());
        config.set("owner_name", gameSettings.getOwnerName());
    }

    @NonNull public GameSettings load(@NonNull UUID levelId, FileConfiguration config) {
        String levelName = config.getString("level_name");
        if (levelName == null) {
            throw new IllegalArgumentException("String \"level_name\" not found");
        }
        String songPlayListName = config.getString("song_play_list_name");
        String songName = config.getString("song_name");
        String ownerIdString = config.getString("owner_id", null);
        if (ownerIdString == null) {
            throw new IllegalArgumentException("String \"owner_id\" not found");
        }
        UUID ownerId = UUID.fromString(ownerIdString);
        String ownerName = config.getString("owner_name");
        if (ownerName == null) {
            throw new IllegalArgumentException("String \"owner_name\" not found");
        }
        return new GameSettings(levelId, levelName, ownerId, ownerName, songPlayListName, songName);
    }
}

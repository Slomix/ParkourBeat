package ru.sortix.parkourbeat.levels.dao.files;

import java.util.UUID;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.Song;

public class GameSettingsDAO {
    public void set(GameSettings gameSettings, FileConfiguration config) {
        config.set("owner_id", gameSettings.getOwnerId().toString());
        config.set("owner_name", gameSettings.getOwnerName());
        config.set("level_name", gameSettings.getLevelName());

        Song song = gameSettings.getSong();
        if (song != null) {
            config.set("song_play_list_name", song.getSongPlaylist());
            config.set("song_name", song.getSongName());
        }
    }

    @NonNull public GameSettings load(@NonNull UUID levelId, FileConfiguration config) {
        String ownerIdString = config.getString("owner_id", null);
        if (ownerIdString == null) {
            throw new IllegalArgumentException("String \"owner_id\" not found");
        }

        UUID ownerId = UUID.fromString(ownerIdString);
        String ownerName = config.getString("owner_name");
        if (ownerName == null) {
            throw new IllegalArgumentException("String \"owner_name\" not found");
        }

        String levelName = config.getString("level_name");
        if (levelName == null) {
            throw new IllegalArgumentException("String \"level_name\" not found");
        }

        Song song = null;
        String songPlayListName = config.getString("song_play_list_name");
        if (songPlayListName != null) {
            String songName = config.getString("song_name");
            if (songName == null) {
                throw new IllegalArgumentException("String \"song_name\" not found");
            }
            song = new Song(songPlayListName, songName);
        }

        return new GameSettings(levelId, levelName, ownerId, ownerName, song);
    }
}

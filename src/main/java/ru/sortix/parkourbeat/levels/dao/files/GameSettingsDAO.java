package ru.sortix.parkourbeat.levels.dao.files;

import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.Song;

import java.util.UUID;

public class GameSettingsDAO {
    public void set(@NonNull GameSettings gameSettings, @NonNull FileConfiguration config) {
        config.set("unique_name", gameSettings.getUniqueName());
        config.set("unique_number", gameSettings.getUniqueNumber());
        config.set("owner_id", gameSettings.getOwnerId().toString());
        config.set("owner_name", gameSettings.getOwnerName());
        config.set("display_name", gameSettings.getRawDisplayName());
        config.set("level_name", null);
        config.set("created_at_mills", gameSettings.getCreatedAtMills());

        Song song = gameSettings.getSong();
        if (song != null) {
            config.set("song_play_list_name", song.getSongPlaylist());
            config.set("song_name", song.getSongName());
        }
    }

    @NonNull
    public GameSettings load(@NonNull UUID uniqueId, @NonNull FileConfiguration config) {
        String uniqueName = config.getString("unique_name", null);

        int uniqueNumber = config.getInt("unique_number", -1);
        if (uniqueNumber < 0) {
            throw new IllegalArgumentException("Int \"unique_number\" not found");
        }

        String ownerIdString = config.getString("owner_id", null);
        if (ownerIdString == null) {
            throw new IllegalArgumentException("String \"owner_id\" not found");
        }

        UUID ownerId = UUID.fromString(ownerIdString);
        String ownerName = config.getString("owner_name");
        if (ownerName == null) {
            throw new IllegalArgumentException("String \"owner_name\" not found");
        }

        String displayName = config.getString("display_name");
        if (displayName == null) {
            throw new IllegalArgumentException("String \"display_name\" not found");
        }

        long createdAtMills = config.getLong("created_at_mills", -1);
        if (createdAtMills < 0) {
            throw new IllegalArgumentException("Long \"created_at_mills\" not found");
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

        return new GameSettings(
            uniqueId, uniqueName, uniqueNumber, ownerId, ownerName, displayName, createdAtMills, song);
    }
}

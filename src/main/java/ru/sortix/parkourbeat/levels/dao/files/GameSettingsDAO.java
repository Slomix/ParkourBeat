package ru.sortix.parkourbeat.levels.dao.files;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.player.music.MusicTracksManager;
import ru.sortix.parkourbeat.player.music.MusicTrack;

import java.util.UUID;

@RequiredArgsConstructor
public class GameSettingsDAO {
    private final @NonNull ParkourBeat plugin;

    public void set(@NonNull GameSettings gameSettings, @NonNull FileConfiguration config) {
        config.set("unique_name", gameSettings.getUniqueName());
        config.set("unique_number", gameSettings.getUniqueNumber());
        config.set("owner_id", gameSettings.getOwnerId().toString());
        config.set("owner_name", gameSettings.getOwnerName());
        config.set("display_name", gameSettings.getDisplayNameLegacy(false));
        config.set("level_name", null);
        config.set("created_at_mills", gameSettings.getCreatedAtMills());
        config.set("custom_physics_enabled", gameSettings.isCustomPhysicsEnabled());

        MusicTrack musicTrack = gameSettings.getMusicTrack();
        if (musicTrack != null) {
            config.set("song_id", musicTrack.getUniqueId());
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

        String displayNameLegacy = config.getString("display_name");
        if (displayNameLegacy == null) {
            throw new IllegalArgumentException("String \"display_name\" not found");
        }
        Component displayName = LegacyComponentSerializer.legacySection().deserialize(displayNameLegacy);

        long createdAtMills = config.getLong("created_at_mills", -1);
        if (createdAtMills < 0) {
            throw new IllegalArgumentException("Long \"created_at_mills\" not found");
        }

        boolean customPhysicsEnabled = config.getBoolean("custom_physics_enabled", true);

        MusicTrack musicTrack = null;
        String songUniqueId = config.getString("song_id", config.getString("song_name"));
        if (songUniqueId != null) {
            musicTrack = this.plugin.get(MusicTracksManager.class).createSongByUniqueId(songUniqueId);
        }

        return new GameSettings(
            uniqueId, uniqueName, uniqueNumber, ownerId, ownerName, displayName, createdAtMills, musicTrack, customPhysicsEnabled);
    }
}

package ru.sortix.parkourbeat.levels;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

public class LevelSettingsManager {
    private final Map<UUID, LevelSettings> levelSettings = new HashMap<>();

    @Getter
    private final LevelSettingDAO levelSettingDAO;

    protected LevelSettingsManager(@NonNull LevelSettingDAO levelSettingDAO) {
        this.levelSettingDAO = levelSettingDAO;
    }

    public void addLevelSettings(@NonNull UUID levelId, @NonNull LevelSettings settings) {
        this.levelSettings.put(levelId, settings);
        this.levelSettingDAO.saveLevelSettings(settings);
    }

    public void unloadLevelSettings(@NonNull UUID levelId) {
        levelSettings.remove(levelId);
    }

    @NonNull public LevelSettings loadLevelSettings(@NonNull UUID levelId) {
        LevelSettings settings = this.levelSettings.get(levelId);
        if (settings != null) return settings;

        settings = this.levelSettingDAO.loadLevelSettings(levelId);
        if (settings == null) {
            throw new IllegalArgumentException("Failed to load settings for level " + levelId);
        }

        this.levelSettings.put(levelId, settings);
        return settings;
    }

    public void saveWorldSettings(@NonNull UUID levelId) {
        LevelSettings settings = this.levelSettings.get(levelId);
        if (settings == null) {
            throw new IllegalStateException(
                    "Failed to save settings for level " + levelId + ": " + "Settings not found");
        }
        this.levelSettingDAO.saveLevelSettings(settings);
    }
}

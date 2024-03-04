package ru.sortix.parkourbeat.levels;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

class LevelSettingsManager {
    private final Map<UUID, LevelSettings> levelSettings = new HashMap<>();
    private final LevelSettingDAO levelSettingDAO;

    protected LevelSettingsManager(@NonNull LevelSettingDAO levelSettingDAO) {
        this.levelSettingDAO = levelSettingDAO;
    }

    public void addLevelSettings(@NonNull UUID levelId, @NonNull LevelSettings settings) {
        levelSettings.put(levelId, settings);
        levelSettingDAO.save(settings);
    }

    public void unloadLevelSettings(@NonNull UUID levelId) {
        levelSettings.remove(levelId);
    }

    public void deleteLevelSettings(@NonNull UUID levelId) {
        LevelSettings settings = levelSettings.remove(levelId);
        if (settings != null) {
            this.levelSettingDAO.delete(levelId);
        }
    }

    @NotNull public LevelSettings loadLevelSettings(@NonNull UUID levelId) {
        LevelSettings settings = this.levelSettings.get(levelId);
        if (settings == null) {
            settings = this.levelSettingDAO.loadLevelSettings(levelId);
            if (settings == null) {
                throw new RuntimeException("Failed to load settings for level " + levelId);
            }
            this.levelSettings.put(levelId, settings);
        }
        return settings;
    }

    public void saveWorldSettings(@NonNull UUID levelId) {
        LevelSettings settings = levelSettings.get(levelId);
        if (settings == null) {
            throw new RuntimeException("Failed to save settings for level " + levelId);
        }
        levelSettingDAO.save(settings);
    }

    @Nullable public LevelSettings getLevelSettings(@NonNull UUID levelId) {
        return levelSettings.get(levelId);
    }
}

package ru.sortix.parkourbeat.levels.dao;

import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

public interface LevelSettingDAO {
    @Nullable String loadLevelName(@NonNull UUID levelId);

    @Nullable LevelSettings loadLevelSettings(@NonNull UUID levelId);

    void saveLevelSettings(LevelSettings settings);

    @NonNull String getLevelWorldName(@NonNull UUID levelId);

    void deleteLevelWorldAndSettings(@NonNull UUID levelId);
}

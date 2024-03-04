package ru.sortix.parkourbeat.levels.dao;

import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

public interface LevelSettingDAO {

    void save(LevelSettings object);

    @Nullable String loadLevelName(@NonNull UUID levelId);

    @Nullable LevelSettings loadLevelSettings(@NonNull UUID levelId);

    void delete(@NonNull UUID levelId);
}

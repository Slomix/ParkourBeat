package ru.sortix.parkourbeat.levels.dao;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

public interface LevelSettingDAO {
    @Nullable LevelSettings loadLevelSettings(@NonNull UUID levelId);

    void saveLevelSettings(@NonNull LevelSettings settings);

    @Nullable World getBukkitWorld(@NonNull UUID levelId);

    void deleteLevelWorldAndSettings(@NonNull UUID levelId);

    @NonNull WorldCreator newWorldCreator(@NonNull UUID levelId);

    boolean isLevelWorld(@NonNull World world);

    @NonNull Collection<GameSettings> loadAllAvailableLevelGameSettingsSync();
}

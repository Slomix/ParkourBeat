package ru.sortix.parkourbeat.levels.dao;

import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

public interface LevelSettingDAO {
    @Nullable LevelSettings loadLevelSettings(@NonNull UUID levelId);

    void saveLevelSettings(LevelSettings settings);

    @Nullable World getBukkitWorld(@NonNull UUID levelId);

    void deleteLevelWorldAndSettings(@NonNull UUID levelId);

    @NonNull WorldCreator newWorldCreator(@NonNull UUID levelId);

    boolean isLevelWorld(@NonNull World world);

    @NonNull Map<String, UUID> loadAllAvailableLevelNamesSync();
}

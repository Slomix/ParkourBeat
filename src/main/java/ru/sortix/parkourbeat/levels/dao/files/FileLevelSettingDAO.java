package ru.sortix.parkourbeat.levels.dao.files;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;

public class FileLevelSettingDAO implements LevelSettingDAO {
    private static final String WORLD_NAME_PREFIX = "pb_level_";
    private static final int WORLD_NAME_PREFIX_LENGTH = WORLD_NAME_PREFIX.length();

    private final File worldsDir;
    private final GameSettingsDAO gameSettingsDAO = new GameSettingsDAO();
    private final WorldSettingsDAO worldSettingsDAO = new WorldSettingsDAO();

    public FileLevelSettingDAO(@NonNull Plugin plugin) {
        this.worldsDir = plugin.getServer().getWorldContainer();
    }

    @NonNull private static File getFile(File levelSettingsDir, String fileName) {
        File worldSettingsFile = new File(levelSettingsDir, fileName);
        if (!worldSettingsFile.exists()) {
            try {
                worldSettingsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return worldSettingsFile;
    }

    private static void saveConfig(FileConfiguration gameSettingsConfig, File gameSettingFile) {
        try {
            gameSettingsConfig.save(gameSettingFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(LevelSettings settings) {
        WorldSettings worldSettings = settings.getWorldSettings();
        GameSettings gameSettings = settings.getGameSettings();

        File settingsDir = getSettingsDirectory(gameSettings.getLevelId());
        File worldSettingsFile = getFile(settingsDir, "world_settings.yml");
        File gameSettingFile = getFile(settingsDir, "game_settings.yml");

        FileConfiguration worldSettingsConfig = YamlConfiguration.loadConfiguration(worldSettingsFile);
        FileConfiguration gameSettingsConfig = YamlConfiguration.loadConfiguration(gameSettingFile);

        gameSettingsDAO.set(gameSettings, gameSettingsConfig);
        worldSettingsDAO.set(worldSettings, worldSettingsConfig);

        saveConfig(gameSettingsConfig, gameSettingFile);
        saveConfig(worldSettingsConfig, worldSettingsFile);
    }

    @Nullable public String loadLevelName(@NonNull UUID levelId) {
        GameSettings gameSettings = this.loadLevelGameSettings(levelId);
        if (gameSettings == null) return null;
        return gameSettings.getLevelName();
    }

    @Override
    @Nullable public LevelSettings loadLevelSettings(@NonNull UUID levelId) {
        WorldSettings worldSettings = this.loadLevelWorldSettings(levelId);
        if (worldSettings == null) return null;
        GameSettings gameSettings = this.loadLevelGameSettings(levelId);
        if (gameSettings == null) return null;
        return new LevelSettings(worldSettings, gameSettings);
    }

    @Nullable private WorldSettings loadLevelWorldSettings(@NonNull UUID levelId) {
        File settingsDir = getSettingsDirectory(levelId);

        File worldSettingsFile = new File(settingsDir, "world_settings.yml");
        if (!worldSettingsFile.isFile()) {
            return null;
        }

        FileConfiguration worldConfig = YamlConfiguration.loadConfiguration(worldSettingsFile);

        World world = Bukkit.getWorld(getWorldDirName(levelId));
        if (world == null) return null;

        return this.worldSettingsDAO.load(worldConfig, world);
    }

    @Nullable private GameSettings loadLevelGameSettings(@NonNull UUID levelId) {
        File settingsDir = getSettingsDirectory(levelId);
        File gameSettingsFile = new File(settingsDir, "game_settings.yml");
        if (!gameSettingsFile.isFile()) {
            return null;
        }
        FileConfiguration gameConfig = YamlConfiguration.loadConfiguration(gameSettingsFile);
        return gameSettingsDAO.load(levelId, gameConfig);
    }

    @Override
    public void delete(@NonNull UUID levelId) {
        File settingsDir = getSettingsDirectory(levelId);
        if (settingsDir.exists()) {
            settingsDir.delete();
        }
    }

    @NonNull private File getSettingsDirectory(@NonNull UUID levelId) {
        File worldDir = new File(this.worldsDir, getWorldDirName(levelId));
        return new File(worldDir, "parkourbeat");
    }

    @NonNull public static String getWorldDirName(@NonNull UUID levelId) {
        return "pb_level_" + levelId;
    }

    @Nullable public static UUID getLevelId(@NonNull String worldName) {
        if (!worldName.startsWith(WORLD_NAME_PREFIX)) return null;
        return UUID.fromString(worldName.substring(WORLD_NAME_PREFIX_LENGTH));
    }
}

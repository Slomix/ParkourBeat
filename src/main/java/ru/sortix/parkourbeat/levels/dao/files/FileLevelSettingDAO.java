package ru.sortix.parkourbeat.levels.dao.files;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;

public class FileLevelSettingDAO implements LevelSettingDAO {
    private final Logger logger;
    private final Server server;
    private final File levelsDir;
    private final GameSettingsDAO gameSettingsDAO;
    private final WorldSettingsDAO worldSettingsDAO;

    public FileLevelSettingDAO(@NonNull Plugin plugin) {
        this.logger = plugin.getLogger();
        this.server = plugin.getServer();
        this.levelsDir = new File(plugin.getDataFolder(), "levels").getAbsoluteFile();
        //noinspection ResultOfMethodCallIgnored
        this.levelsDir.mkdirs();
        this.gameSettingsDAO = new GameSettingsDAO();
        this.worldSettingsDAO = new WorldSettingsDAO();
    }

    @Override
    @Nullable public LevelSettings loadLevelSettings(@NonNull UUID levelId) {
        WorldSettings worldSettings = this.loadLevelWorldSettings(levelId);
        if (worldSettings == null) return null;
        GameSettings gameSettings = this.loadLevelGameSettings(levelId);
        if (gameSettings == null) return null;
        return new LevelSettings(worldSettings, gameSettings);
    }

    @Override
    public void saveLevelSettings(LevelSettings settings) {
        try {
            WorldSettings worldSettings = settings.getWorldSettings();
            GameSettings gameSettings = settings.getGameSettings();

            File worldSettingsFile = getFile(gameSettings.getLevelId(), "world_settings.yml");
            File gameSettingFile = getFile(gameSettings.getLevelId(), "game_settings.yml");

            FileConfiguration worldSettingsConfig =
                    YamlConfiguration.loadConfiguration(worldSettingsFile);
            FileConfiguration gameSettingsConfig = YamlConfiguration.loadConfiguration(gameSettingFile);

            gameSettingsDAO.set(gameSettings, gameSettingsConfig);
            worldSettingsDAO.set(worldSettings, worldSettingsConfig);

            saveConfig(gameSettingsConfig, gameSettingFile);
            saveConfig(worldSettingsConfig, worldSettingsFile);
        } catch (Exception e) {
            this.logger.log(
                    Level.SEVERE, "Unable to save level " + settings.getGameSettings().getLevelId(), e);
        }
    }

    @Override
    @Nullable public World getBukkitWorld(@NonNull UUID levelId) {
        return this.server.getWorld(this.getBukkitWorldName(levelId));
    }

    @Override
    public void deleteLevelWorldAndSettings(@NonNull UUID levelId) {
        File worldFolder = this.getBukkitWorldDirectory(levelId);
        if (!worldFolder.isDirectory()) return;
        deleteDirectory(worldFolder);
    }

    @NonNull private File getFile(@NonNull UUID levelId, @NonNull String fileName) throws IOException {
        File levelSettingsDir = getSettingsDirectory(levelId);
        if (!levelSettingsDir.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            levelSettingsDir.mkdirs();
        }
        File worldSettingsFile = new File(levelSettingsDir, fileName);
        if (!worldSettingsFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            worldSettingsFile.createNewFile();
        }
        return worldSettingsFile;
    }

    private void saveConfig(
            @NonNull FileConfiguration gameSettingsConfig, @NonNull File gameSettingFile)
            throws IOException {
        gameSettingsConfig.save(gameSettingFile);
    }

    private static void deleteDirectory(@NonNull File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        directory.delete();
    }

    @Nullable private WorldSettings loadLevelWorldSettings(@NonNull UUID levelId) {
        File settingsDir = getSettingsDirectory(levelId);

        File worldSettingsFile = new File(settingsDir, "world_settings.yml");
        if (!worldSettingsFile.isFile()) {
            return null;
        }

        FileConfiguration worldConfig = YamlConfiguration.loadConfiguration(worldSettingsFile);

        World world = this.server.getWorld(getBukkitWorldName(levelId));
        if (world == null) return null;

        try {
            return this.worldSettingsDAO.load(worldConfig, world);
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "Unable to load world_settings.yml of level " + levelId, e);
            return null;
        }
    }

    @Nullable private GameSettings loadLevelGameSettings(@NonNull UUID levelId) {
        File settingsDir = getSettingsDirectory(levelId);
        File gameSettingsFile = new File(settingsDir, "game_settings.yml");
        if (!gameSettingsFile.isFile()) {
            this.logger.warning("Not a file: " + gameSettingsFile.getAbsolutePath());
            return null;
        }
        try {
            FileConfiguration gameConfig = YamlConfiguration.loadConfiguration(gameSettingsFile);
            return this.gameSettingsDAO.load(levelId, gameConfig);
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "Unable to load game_settings.yml of level " + levelId, e);
            return null;
        }
    }

    @NonNull private File getSettingsDirectory(@NonNull UUID levelId) {
        return new File(getBukkitWorldDirectory(levelId), "parkourbeat");
    }

    @NonNull public File getBukkitWorldDirectory(@NonNull UUID levelId) {
        return new File(this.levelsDir, levelId.toString());
    }

    @Override
    @NonNull public WorldCreator newWorldCreator(@NonNull UUID levelId) {
        return new WorldCreator(this.getBukkitWorldName(levelId));
    }

    @Override
    public boolean isLevelWorld(@NonNull World world) {
        // TODO Optimize it
        return world.getWorldFolder().getParentFile().equals(this.levelsDir);
    }

    @NonNull private String getBukkitWorldName(@NonNull UUID levelId) {
        return this.getBukkitWorldDirectory(levelId).getAbsolutePath();
    }

    @NonNull public Map<String, UUID> loadAllAvailableLevelNamesSync() {
        Map<String, UUID> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        File worldsDirectory = this.levelsDir;
        if (!worldsDirectory.isDirectory()) return result;

        File[] files = worldsDirectory.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (!file.isDirectory()) {
                this.logger.warning("Not a directory: " + file.getAbsolutePath());
                continue;
            }
            UUID levelId;
            try {
                levelId = UUID.fromString(file.getName());
            } catch (IllegalArgumentException e) {
                this.logger.warning(
                        "Unable to parse level UUID by world dir name: " + file.getAbsolutePath());
                continue;
            }
            GameSettings gameSettings = this.loadLevelGameSettings(levelId);
            if (gameSettings == null) {
                this.logger.warning("Unable to load name of level " + levelId);
                continue;
            }
            String levelName = gameSettings.getLevelName();
            if (result.put(levelName, levelId) != null) {
                this.logger.warning("Duplicate level name: " + levelName);
            }
        }
        return result;
    }
}

package ru.sortix.parkourbeat.levels.dao.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;

public class FileLevelSettingDAO implements LevelSettingDAO {
    private final Logger logger;
    private final Server server;

    private final Path worldsContainerPath;
    private final File levelsDirRelativeDir;
    private final File levelsDirAbsoluteFile;

    private final GameSettingsDAO gameSettingsDAO;
    private final WorldSettingsDAO worldSettingsDAO;

    public FileLevelSettingDAO(@NonNull LevelsManager levelsManager) {
        Plugin plugin = levelsManager.getPlugin();
        this.logger = plugin.getLogger();
        this.server = plugin.getServer();

        this.worldsContainerPath = this.server.getWorldContainer().toPath();
        this.levelsDirRelativeDir = new File(plugin.getDataFolder(), "levels");
        this.levelsDirAbsoluteFile = this.levelsDirRelativeDir.getAbsoluteFile();
        //noinspection ResultOfMethodCallIgnored
        this.levelsDirAbsoluteFile.mkdirs();

        this.gameSettingsDAO = new GameSettingsDAO();
        this.worldSettingsDAO = new WorldSettingsDAO();
    }

    @Override
    @Nullable public LevelSettings loadLevelSettings(@NonNull UUID levelId, @Nullable GameSettings gameSettings) {
        WorldSettings worldSettings = this.loadLevelWorldSettings(levelId);
        if (worldSettings == null) return null;
        if (gameSettings == null) gameSettings = this.loadLevelGameSettings(levelId);
        if (gameSettings == null) return null;
        return new LevelSettings(worldSettings, gameSettings);
    }

    @Override
    public void saveLevelSettings(@NonNull LevelSettings settings) {
        try {
            WorldSettings worldSettings = settings.getWorldSettings();
            GameSettings gameSettings = settings.getGameSettings();
            UUID levelId = gameSettings.getUniqueId();

            FileConfiguration gameSettingsConfig = new YamlConfiguration();
            FileConfiguration worldSettingsConfig = new YamlConfiguration();

            this.gameSettingsDAO.set(gameSettings, gameSettingsConfig);
            this.worldSettingsDAO.set(worldSettings, worldSettingsConfig);

            saveConfig(gameSettingsConfig, getFile(levelId, "game_settings.yml"));
            saveConfig(worldSettingsConfig, getFile(levelId, "world_settings.yml"));
        } catch (Exception e) {
            this.logger.log(
                    Level.SEVERE,
                    "Unable to save level " + settings.getGameSettings().getUniqueId(),
                    e);
        }
    }

    @Override
    @Nullable public World getBukkitWorld(@NonNull UUID levelId) {
        return this.server.getWorld(this.getBukkitWorldName(levelId));
    }

    @Override
    public void deleteLevelWorldAndSettings(@NonNull UUID levelId) {
        File worldFolder = this.getBukkitWorldDirectory(levelId).getAbsoluteFile();
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

    private void saveConfig(@NonNull FileConfiguration gameSettingsConfig, @NonNull File gameSettingFile)
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
        return new File(getBukkitWorldDirectory(levelId).getAbsoluteFile(), "parkourbeat");
    }

    @NonNull public File getBukkitWorldDirectory(@NonNull UUID levelId) {
        return new File(this.levelsDirRelativeDir, levelId.toString());
    }

    @Override
    @NonNull public WorldCreator newWorldCreator(@NonNull UUID levelId) {
        return new WorldCreator(this.getBukkitWorldName(levelId));
    }

    @Override
    public boolean isLevelWorld(@NonNull World world) {
        try {
            return world.getWorldFolder().getParentFile().getCanonicalFile().equals(this.levelsDirAbsoluteFile);
        } catch (IOException e) {
            return false;
        }
    }

    @NonNull private String getBukkitWorldName(@NonNull UUID levelId) {
        return this.worldsContainerPath
                .relativize(this.getBukkitWorldDirectory(levelId).toPath())
                .toFile()
                .getPath()
                .replace("\\", "/"); // fix Windows issues
    }

    @NonNull public Collection<GameSettings> loadAllAvailableLevelGameSettingsSync() {
        List<GameSettings> result = new ArrayList<>();

        if (!this.levelsDirRelativeDir.isDirectory()) {
            this.logger.warning("Levels directory not found: " + this.levelsDirRelativeDir.getAbsolutePath());
            return result;
        }

        File[] files = this.levelsDirRelativeDir.listFiles();
        if (files == null) {
            this.logger.warning(
                    "Unable to get levels directory content: " + this.levelsDirRelativeDir.getAbsolutePath());
            return result;
        }

        for (File file : files) {
            if (!file.isDirectory()) {
                this.logger.warning("Not a directory: " + file.getAbsolutePath());
                continue;
            }
            UUID levelId;
            try {
                levelId = UUID.fromString(file.getName());
            } catch (IllegalArgumentException e) {
                this.logger.warning("Unable to parse level UUID by world dir name: " + file.getAbsolutePath());
                continue;
            }
            GameSettings gameSettings = this.loadLevelGameSettings(levelId);
            if (gameSettings == null) {
                this.logger.warning("Unable to load name of level " + levelId);
                continue;
            }
            result.add(gameSettings);
        }
        return result;
    }
}

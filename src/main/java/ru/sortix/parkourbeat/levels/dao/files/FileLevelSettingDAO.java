package ru.sortix.parkourbeat.levels.dao.files;

import java.io.File;
import java.io.IOException;
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
    private static final String WORLD_NAME_PREFIX = "pb_level_";
    private static final int WORLD_NAME_PREFIX_LENGTH = WORLD_NAME_PREFIX.length();

    private final Logger logger;
    private final Server server;
    private final File worldsDir;
    private final GameSettingsDAO gameSettingsDAO;
    private final WorldSettingsDAO worldSettingsDAO;

    public FileLevelSettingDAO(@NonNull Plugin plugin) {
        this.logger = plugin.getLogger();
        this.server = plugin.getServer();
        this.worldsDir = plugin.getServer().getWorldContainer();
        this.gameSettingsDAO = new GameSettingsDAO();
        this.worldSettingsDAO = new WorldSettingsDAO();
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
    @NonNull public String getLevelWorldName(@NonNull UUID levelId) {
        return FileLevelSettingDAO.getWorldDirName(levelId);
    }

    @Override
    public void deleteLevelWorldAndSettings(@NonNull UUID levelId) {
        String worldDirName = FileLevelSettingDAO.getWorldDirName(levelId);
        File worldFolder = new File(this.server.getWorldContainer(), worldDirName);
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
        directory.delete();
    }

    @Nullable private WorldSettings loadLevelWorldSettings(@NonNull UUID levelId) {
        File settingsDir = getSettingsDirectory(levelId);

        File worldSettingsFile = new File(settingsDir, "world_settings.yml");
        if (!worldSettingsFile.isFile()) {
            return null;
        }

        FileConfiguration worldConfig = YamlConfiguration.loadConfiguration(worldSettingsFile);

        World world = this.server.getWorld(getWorldDirName(levelId));
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
        return new File(getWorldDirectory(levelId), "parkourbeat");
    }

    @NonNull public File getWorldDirectory(@NonNull UUID levelId) {
        return new File(this.worldsDir, getWorldDirName(levelId));
    }

    @NonNull public static WorldCreator newWorldCreator(@NonNull UUID levelId) {
        return new WorldCreator(FileLevelSettingDAO.getWorldDirName(levelId));
    }

    @Nullable public static UUID getLevelId(@NonNull String worldName) {
        if (!worldName.startsWith(WORLD_NAME_PREFIX)) return null;
        return UUID.fromString(worldName.substring(WORLD_NAME_PREFIX_LENGTH));
    }

    @NonNull private static String getWorldDirName(@NonNull UUID levelId) {
        return WORLD_NAME_PREFIX + levelId;
    }
}

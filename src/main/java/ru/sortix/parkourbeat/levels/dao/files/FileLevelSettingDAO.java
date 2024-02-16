package ru.sortix.parkourbeat.levels.dao.files;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;

public class FileLevelSettingDAO implements LevelSettingDAO {

  private final File settingsDirectory;
  private final GameSettingsDAO gameSettingsDAO = new GameSettingsDAO();
  private final WorldSettingsDAO worldSettingsDAO = new WorldSettingsDAO();

  public FileLevelSettingDAO(String directory) {
    File worldDir = new File(directory);
    if (!worldDir.exists()) {
      worldDir.mkdir();
    }
    settingsDirectory = worldDir;
  }

  @NotNull private static File getFile(File levelSettingsDir, String fileName) {
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

    File levelSettingsDir = new File(settingsDirectory, worldSettings.getWorld().getName());
    if (!levelSettingsDir.exists()) {
      levelSettingsDir.mkdir();
    }

    File worldSettingsFile = getFile(levelSettingsDir, "world_settings.yml");
    File gameSettingFile = getFile(levelSettingsDir, "game_settings.yml");

    FileConfiguration worldSettingsConfig = YamlConfiguration.loadConfiguration(worldSettingsFile);
    FileConfiguration gameSettingsConfig = YamlConfiguration.loadConfiguration(gameSettingFile);

    gameSettingsDAO.set(gameSettings, gameSettingsConfig);
    worldSettingsDAO.set(worldSettings, worldSettingsConfig);

    saveConfig(gameSettingsConfig, gameSettingFile);
    saveConfig(worldSettingsConfig, worldSettingsFile);
  }

  @Override
  @Nullable public LevelSettings load(String levelName) {
    File settingsDir = new File(settingsDirectory, levelName);
    File worldSettingsFile = new File(settingsDir, "world_settings.yml");
    File gameSettingsFile = new File(settingsDir, "game_settings.yml");
    if (!settingsDir.exists() || !gameSettingsFile.exists() || !worldSettingsFile.exists()) {
      return null;
    }
    FileConfiguration worldConfig = YamlConfiguration.loadConfiguration(worldSettingsFile);
    FileConfiguration gameConfig = YamlConfiguration.loadConfiguration(gameSettingsFile);

    WorldSettings worldSettings = worldSettingsDAO.load(worldConfig, Bukkit.getWorld(levelName));
    GameSettings gameSettings = gameSettingsDAO.load(gameConfig);

    return new LevelSettings(worldSettings, gameSettings);
  }

  @Override
  public void delete(String levelName) {
    File settingsFile = new File(settingsDirectory, levelName);
    if (settingsFile.exists()) {
      settingsFile.delete();
    }
  }
}

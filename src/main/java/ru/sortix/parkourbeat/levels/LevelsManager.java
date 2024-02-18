package ru.sortix.parkourbeat.levels;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

public class LevelsManager {

	private final Set<String> levels = new HashSet<>();
	private final Map<String, Level> loadedLevels = new HashMap<>();
	private final LevelSettingsManager levelsSettings;
	private final JavaPlugin plugin;

	public LevelsManager(JavaPlugin plugin, LevelSettingDAO worldSettingDAO) {
		this.levelsSettings = new LevelSettingsManager(worldSettingDAO);
		this.plugin = plugin;
		init();
	}

	private void init() {
		File worldDirectory = Bukkit.getWorldContainer();
		if (worldDirectory.exists() && worldDirectory.isDirectory()) {
			Arrays.stream(worldDirectory.listFiles())
					.filter(File::isDirectory)
					.map(File::getName)
					.forEach(levels::add);
			levels.remove(Settings.getLobbySpawn().getWorld().getName());
		}
	}

	public Level createLevel(String name, World.Environment environment, String owner) {
		if (levels.contains(name)) {
			return null;
		}

		File source = new File(plugin.getDataFolder(), "defaultWorld");
		File target = new File(Bukkit.getWorldContainer(), name);
		try {
			Files.walk(source.toPath())
					.forEach(
							sourcePath -> {
								Path targetPath = target.toPath().resolve(source.toPath().relativize(sourcePath));
								try {
									Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
								} catch (IOException e) {
									e.printStackTrace();
								}
							});
		} catch (IOException e) {
			e.printStackTrace();
		}

		// TODO: replace Bukkit worlds to SWM
		// WorldCreator(name).environment(environment)) - creates a new world not a copy

		World world = Bukkit.createWorld(new WorldCreator(name));

		world.setAutoSave(false);
		levels.add(name);
		LevelSettings levelSettings = LevelSettings.create(world, owner);
		levelsSettings.addLevelSettings(name, levelSettings);
		Level level = new Level(name, world, levelSettings);
		level.setEditing(true);
		return level;
	}

	@Nullable public LevelSettings getLevelSettings(World world) {
		return levelsSettings.getLevelSettings(world.getName());
	}

	public void deleteLevel(String name) {
		World world = Bukkit.getWorld(name);
		if (world != null) {
			Bukkit.unloadWorld(name, false);
		}
		File worldFolder = new File(Bukkit.getWorldContainer(), name);
		deleteDirectory(worldFolder);
		levels.remove(name);
		levelsSettings.deleteLevelSettings(name);
	}

	public Level loadLevel(String name) {
		if (isLevelLoaded(name)) {
			return getLevelWorld(name);
		}
		World world = Bukkit.createWorld(new WorldCreator(name));
		world.setAutoSave(false);
		Level loadedLevel = new Level(name, world, levelsSettings.loadLevelSettings(name));
		loadedLevels.put(name, loadedLevel);
		return loadedLevel;
	}

	public void unloadLevel(String name) {
		if (!isLevelLoaded(name)) {
			return;
		}
		levelsSettings.unloadLevelSettings(name);
		loadedLevels.remove(name);
		Bukkit.unloadWorld(name, false);
	}

	public void saveLevel(Level level) {
		level.getWorld().save();
		levelsSettings.saveWorldSettings(level.getName());
	}

	public boolean isLevelLoaded(String name) {
		return loadedLevels.containsKey(name);
	}

	@Nullable public Level getLevelWorld(String name) {
		return loadedLevels.get(name);
	}

	@NotNull public Set<String> getAllLevels() {
		return levels;
	}

	private void deleteDirectory(File directory) {
		File[] allContents = directory.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		directory.delete();
	}

	public List<String> getLoadedLevels() {
		return loadedLevels.keySet().stream().sorted().collect(Collectors.toList());
	}
}

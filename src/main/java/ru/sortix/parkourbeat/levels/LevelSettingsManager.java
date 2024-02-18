package ru.sortix.parkourbeat.levels;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

class LevelSettingsManager {
	private final Map<String, LevelSettings> levelSettings = new HashMap<>();
	private final LevelSettingDAO levelSettingDAO;

	protected LevelSettingsManager(LevelSettingDAO levelSettingDAO) {
		this.levelSettingDAO = levelSettingDAO;
	}

	public void addLevelSettings(String name, LevelSettings settings) {
		levelSettings.put(name, settings);
		levelSettingDAO.save(settings);
	}

	public void unloadLevelSettings(String name) {
		levelSettings.remove(name);
	}

	public void deleteLevelSettings(String name) {
		LevelSettings settings = levelSettings.remove(name);
		if (settings != null) {
			levelSettingDAO.delete(name);
		}
	}

	@NotNull public LevelSettings loadLevelSettings(String name) {
		LevelSettings settings = levelSettings.get(name);
		if (settings == null) {
			settings = levelSettingDAO.load(name);
			if (settings == null) {
				throw new RuntimeException("Failed to load world settings for world " + name);
			}
			levelSettings.put(name, settings);
		}
		return settings;
	}

	public void saveWorldSettings(String name) {
		LevelSettings settings = levelSettings.get(name);
		if (settings == null) {
			throw new RuntimeException("Failed to save world settings for world " + name);
		}
		levelSettingDAO.save(settings);
	}

	public LevelSettings getLevelSettings(String name) {
		return levelSettings.get(name);
	}
}

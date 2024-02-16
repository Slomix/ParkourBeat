package ru.sortix.parkourbeat.levels.dao;

import ru.sortix.parkourbeat.levels.settings.LevelSettings;

public interface LevelSettingDAO {

  void save(LevelSettings object);

  LevelSettings load(String name);

  void delete(String name);
}

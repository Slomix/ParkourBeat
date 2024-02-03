package parkourbeat.levels.dao;

import parkourbeat.levels.settings.LevelSettings;

public interface LevelSettingDAO {

    void save(LevelSettings object);

    LevelSettings load(String name);

    void delete(String name);

}

package ru.sortix.parkourbeat.levels;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class LevelsManager {

    private final Set<String> levels = new HashSet<>();
    private final LevelSettingsManager levelsSettings;

    public LevelsManager(LevelSettingDAO worldSettingDAO) {
        this.levelsSettings = new LevelSettingsManager(worldSettingDAO);
        init();
    }

    private void init() {
        File worldDirectory = Bukkit.getWorldContainer();
        if (worldDirectory.exists() && worldDirectory.isDirectory()) {
            Arrays.stream(worldDirectory.listFiles())
                    .filter(File::isDirectory)
                    .map(File::getName)
                    .forEach(levels::add);
        }
    }

    public World createLevel(String name, WorldType type) {
        if (levels.contains(name)) {
            return null;
        }
        World world = Bukkit.createWorld(new WorldCreator(name).type(type).generator(new VoidGenerator()));
        world.setAutoSave(false);
        levels.add(name);
        levelsSettings.addLevelSettings(name, LevelSettings.create(world));
        return world;
    }

    @NotNull
    public LevelSettings loadLevelSettings(World world) {
        return levelsSettings.loadLevelSettings(world.getName());
    }

    @Nullable
    public LevelSettings getLevelSettings(World world) {
        return levelsSettings.getLevelSettings(world.getName());
    }

    public void deleteLevel(String name) {
        World world = Bukkit.getWorld(name);
        File worldFolder = world.getWorldFolder();
        Bukkit.unloadWorld(name, false);
        deleteDirectory(worldFolder);
        levels.remove(name);
        levelsSettings.deleteLevelSettings(name);
    }

    public World loadLevel(String name) {
        if (isLevelLoaded(name)) {
            return getLevelWorld(name);
        }
        World world = Bukkit.createWorld(new WorldCreator(name));
        world.setAutoSave(false);
        levelsSettings.loadLevelSettings(name);
        return world;
    }

    public void unloadLevel(String name) {
        if (!isLevelLoaded(name)) {
            return;
        }
        levelsSettings.unloadLevelSettings(name);
        Bukkit.unloadWorld(name, false);
    }

    public void saveLevel(World world) {
        world.save();
        String name = world.getName();
        levelsSettings.saveWorldSettings(name);
    }

    public boolean isLevelLoaded(String name) {
        return Bukkit.getWorld(name) != null;
    }

    @Nullable
    public World getLevelWorld(String name) {
        return Bukkit.getWorld(name);
    }

    @NotNull
    public Set<String> getAllLevels() {
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
        List<String> worldNames = Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
        worldNames.remove(Settings.getExitLocation().getWorld().getName());
        return worldNames;
    }

    public static class VoidGenerator extends ChunkGenerator {

        @Override
        public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid){
            byte[][] result = new byte[world.getMaxHeight() / 16][];
            return result;
        }

    }

}

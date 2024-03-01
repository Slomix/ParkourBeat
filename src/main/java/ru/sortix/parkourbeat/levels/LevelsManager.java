package ru.sortix.parkourbeat.levels;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.GameRule;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

public class LevelsManager {
    @Getter private final Plugin plugin;
    private final WorldsManager worldsManager;
    private final LevelSettingsManager levelsSettings;
    private final Set<String> levels = new HashSet<>();
    private final Map<String, Level> loadedLevels = new HashMap<>();

    public LevelsManager(
            @NonNull Plugin plugin,
            @NonNull WorldsManager worldsManager,
            @NonNull LevelSettingDAO worldSettingDAO) {
        this.plugin = plugin;
        this.worldsManager = worldsManager;
        this.levelsSettings = new LevelSettingsManager(worldSettingDAO);
        loadLevels();
    }

    private void loadLevels() {
        File worldDirectory = this.plugin.getServer().getWorldContainer();
        if (!worldDirectory.isDirectory()) return;

        File[] files = worldDirectory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.isDirectory()) continue;
            this.levels.add(file.getName());
        }
        // TODO Exclude other third-party worlds
        this.levels.remove(Settings.getLobbySpawn().getWorld().getName());
    }

    @NonNull public CompletableFuture<Level> createLevel(
            @NonNull String worldName, @NonNull World.Environment environment, @NonNull String owner) {
        CompletableFuture<Level> result = new CompletableFuture<>();

        if (this.levels.contains(worldName)) {
            result.completeExceptionally(new IllegalArgumentException("World already created"));
            return result;
        }

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.generator(this.worldsManager.getEmptyGenerator());
        if (false) worldCreator.environment(environment); // creates a new world not a copy

        File worldDir = new File(this.plugin.getDataFolder(), "pb_default_level");

        this.worldsManager
                .createWorldFromCustomDirectory(worldCreator, worldDir)
                .thenAccept(
                        world -> {
                            prepareLevelWorld(world, true);

                            LevelSettings levelSettings = LevelSettings.create(world, owner);
                            Level level = new Level(worldName, world, levelSettings);
                            level.setEditing(true);

                            this.levels.add(worldName);
                            this.levelsSettings.addLevelSettings(worldName, levelSettings);

                            result.complete(level);
                        });
        return result;
    }

    @Nullable public LevelSettings getLevelSettings(World world) {
        return levelsSettings.getLevelSettings(world.getName());
    }

    public void deleteLevel(String name) {
        Server server = this.plugin.getServer();
        World world = server.getWorld(name);
        if (world != null) {
            server.unloadWorld(name, false);
        }
        File worldFolder = new File(server.getWorldContainer(), name);
        deleteDirectory(worldFolder);
        levels.remove(name);
        levelsSettings.deleteLevelSettings(name);
    }

    @NonNull public CompletableFuture<Level> loadLevel(@NonNull String name) {
        CompletableFuture<Level> result = new CompletableFuture<>();

        if (isLevelLoaded(name)) {
            result.complete(getLevelWorld(name));
            return result;
        }

        WorldCreator worldCreator = new WorldCreator(name);
        this.worldsManager
                .createWorldFromDefaultContainer(worldCreator, this.worldsManager.getSyncExecutor())
                .thenAccept(
                        world -> {
                            this.prepareLevelWorld(world, false);

                            Level loadedLevel =
                                    new Level(name, world, this.levelsSettings.loadLevelSettings(name));
                            this.loadedLevels.put(name, loadedLevel);

                            result.complete(loadedLevel);
                        });
        return result;
    }

    public void unloadLevel(String name) {
        if (!isLevelLoaded(name)) {
            return;
        }
        levelsSettings.unloadLevelSettings(name);
        loadedLevels.remove(name);
        this.plugin.getServer().unloadWorld(name, false);
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

    public void prepareLevelWorld(@NonNull World world, boolean updateGameRules) {
        world.setKeepSpawnInMemory(false);
        world.setAutoSave(false);

        if (!updateGameRules) return;

        setBooleanGameRule(world, "ANNOUNCE_ADVANCEMENTS", false);
        if (false) setBooleanGameRule(world, "COMMAND_BLOCK_OUTPUT", false);
        setBooleanGameRule(world, "DISABLE_ELYTRA_MOVEMENT_CHECK", true);
        setBooleanGameRule(world, "DO_DAYLIGHT_CYCLE", false);
        setBooleanGameRule(world, "DO_ENTITY_DROPS", false);
        setBooleanGameRule(world, "DO_FIRE_TICK", false);
        setBooleanGameRule(world, "DO_LIMITED_CRAFTING", true);
        setBooleanGameRule(world, "DO_MOB_LOOT", false);
        setBooleanGameRule(world, "DO_MOB_SPAWNING", false);
        setBooleanGameRule(world, "DO_TILE_DROPS", false);
        setBooleanGameRule(world, "DO_WEATHER_CYCLE", false);
        setBooleanGameRule(world, "KEEP_INVENTORY", true);
        setBooleanGameRule(world, "LOG_ADMIN_COMMANDS", true);
        setBooleanGameRule(world, "MOB_GRIEFING", false);
        setBooleanGameRule(world, "NATURAL_REGENERATION", false);
        setBooleanGameRule(
                world, "REDUCED_DEBUG_INFO", false); // Should be switched to "true" after level publication
        if (false) setBooleanGameRule(world, "SEND_COMMAND_FEEDBACK", false);
        setBooleanGameRule(world, "SHOW_DEATH_MESSAGES", false);
        setBooleanGameRule(world, "SPECTATORS_GENERATE_CHUNKS", false);
        setBooleanGameRule(world, "DISABLE_RAIDS", true);
        setBooleanGameRule(world, "DO_INSOMNIA", false);
        setBooleanGameRule(world, "DO_IMMEDIATE_RESPAWN", true);
        setBooleanGameRule(world, "DROWNING_DAMAGE", false);
        setBooleanGameRule(world, "FALL_DAMAGE", false);
        setBooleanGameRule(world, "FIRE_DAMAGE", false);
        setBooleanGameRule(world, "DO_PATROL_SPAWNING", false);
        setBooleanGameRule(world, "DO_TRADER_SPAWNING", false);
        setBooleanGameRule(world, "FORGIVE_DEAD_PLAYERS", true);
        setBooleanGameRule(world, "UNIVERSAL_ANGER", false);
        setIntegerGameRule(world, "RANDOM_TICK_SPEED", 0);
        setIntegerGameRule(world, "SPAWN_RADIUS", 0);
        if (false) setIntegerGameRule(world, "MAX_ENTITY_CRAMMING", 24);
        if (false) setIntegerGameRule(world, "MAX_COMMAND_CHAIN_LENGTH", 65536);
    }

    private void setBooleanGameRule(@NonNull World world, @NonNull String name, boolean newValue) {
        GameRule<?> byName = GameRule.getByName(name);
        if (byName == null) return;
        //noinspection unchecked
        world.setGameRule((GameRule<Boolean>) byName, newValue);
    }

    private void setIntegerGameRule(@NonNull World world, @NonNull String name, int newValue) {
        GameRule<?> byName = GameRule.getByName(name);
        if (byName == null) return;
        //noinspection unchecked
        world.setGameRule((GameRule<Integer>) byName, newValue);
    }
}

package ru.sortix.parkourbeat.levels;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.utils.java.ClassUtils;

public class LevelsManager {
    @Getter private final Plugin plugin;
    private final WorldsManager worldsManager;
    @Getter private final LevelSettingsManager levelsSettings;
    private final Map<String, UUID> availableLevelIdsByName =
            new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<UUID, Level> loadedLevels = new HashMap<>();
    private final boolean gameRulesSupport = ClassUtils.isClassPresent("org.bukkit.GameRule");

    public LevelsManager(
            @NonNull Plugin plugin,
            @NonNull WorldsManager worldsManager,
            @NonNull LevelSettingDAO levelSettingDAO) {
        this.plugin = plugin;
        this.worldsManager = worldsManager;
        this.levelsSettings = new LevelSettingsManager(levelSettingDAO);
        loadAvailableLevelNames();
    }

    private void loadAvailableLevelNames() {
        this.availableLevelIdsByName.putAll(
                this.levelsSettings.getLevelSettingDAO().loadAllAvailableLevelNamesSync());
        for (Map.Entry<String, UUID> entry : this.availableLevelIdsByName.entrySet()) {
            this.plugin.getLogger().info("Loaded world " + entry.getValue() + ": " + entry.getKey());
        }
    }

    @NonNull public CompletableFuture<Level> createLevel(
            @NonNull String levelName,
            @NonNull World.Environment environment,
            @NonNull UUID ownerId,
            @NonNull String ownerName) {

        CompletableFuture<Level> result = new CompletableFuture<>();
        if (this.availableLevelIdsByName.containsKey(levelName)) {
            result.complete(null);
            return result;
        }

        UUID levelId = UUID.randomUUID();
        WorldCreator worldCreator = this.levelsSettings.getLevelSettingDAO().newWorldCreator(levelId);
        worldCreator.generator(this.worldsManager.getEmptyGenerator());
        if (false) worldCreator.environment(environment); // creates a new world not a copy

        File worldDir = new File(this.plugin.getDataFolder(), "pb_default_level");

        this.worldsManager
                .createWorldFromCustomDirectory(worldCreator, worldDir)
                .thenAccept(
                        world -> {
                            prepareLevelWorld(world, true);

                            LevelSettings levelSettings =
                                    LevelSettings.create(levelId, levelName, world, ownerId, ownerName);
                            Level level = new Level(levelId, levelName, world, levelSettings);
                            level.setEditing(true);

                            this.availableLevelIdsByName.put(level.getLevelName(), levelId);
                            this.levelsSettings.addLevelSettings(levelId, levelSettings);

                            result.complete(level);
                        });
        return result;
    }

    @NonNull public CompletableFuture<Level> loadLevel(@NonNull UUID levelId) {
        CompletableFuture<Level> result = new CompletableFuture<>();

        if (isLevelLoaded(levelId)) {
            result.complete(getLoadedLevel(levelId));
            return result;
        }

        WorldCreator worldCreator = this.levelsSettings.getLevelSettingDAO().newWorldCreator(levelId);
        this.worldsManager
                .createWorldFromDefaultContainer(worldCreator, this.worldsManager.getSyncExecutor())
                .thenAccept(
                        world -> {
                            try {
                                this.prepareLevelWorld(world, false);

                                LevelSettings levelSettings = this.levelsSettings.loadLevelSettings(levelId);
                                String levelName = levelSettings.getGameSettings().getLevelName();
                                Level loadedLevel = new Level(levelId, levelName, world, levelSettings);
                                this.loadedLevels.put(levelId, loadedLevel);

                                result.complete(loadedLevel);
                            } catch (Exception e) {
                                e.printStackTrace();
                                result.complete(null);
                            }
                        });
        return result;
    }

    public boolean deleteLevel(@NonNull Level level) {
        UUID levelId = level.getLevelId();
        if (!this.unloadLevel(levelId)) return false;

        this.availableLevelIdsByName.remove(level.getLevelName());
        this.levelsSettings.getLevelSettingDAO().deleteLevelWorldAndSettings(levelId);
        return true;
    }

    public boolean unloadLevel(@NonNull UUID levelId) {
        LevelSettingDAO dao = this.levelsSettings.getLevelSettingDAO();

        if (!this.loadedLevels.containsKey(levelId)) return true;

        World world = dao.getBukkitWorld(levelId);
        boolean success = true;
        if (world != null) {
            for (Player player : world.getPlayers()) {
                player.sendMessage("Уровень, на которым вы находились, был удалён");
                player.teleport(Settings.getLobbySpawn());
            }
            success = this.plugin.getServer().unloadWorld(world, false);
        }

        if (!success) {
            this.plugin.getLogger().severe("Unable to unload world: " + world);
            return false;
        }

        this.levelsSettings.unloadLevelSettings(levelId);
        this.loadedLevels.remove(levelId);
        return true;
    }

    public void saveLevel(Level level) {
        try {
            level.getWorld().save();
        } catch (Exception e) {
            this.plugin
                    .getLogger()
                    .log(
                            java.util.logging.Level.SEVERE,
                            "Unable to save world " + level.getWorld().getName(),
                            e);
        }
        levelsSettings.saveWorldSettings(level.getLevelId());
    }

    public boolean isLevelLoaded(@NonNull UUID levelId) {
        return this.loadedLevels.containsKey(levelId);
    }

    @Nullable public Level getLoadedLevel(@NonNull UUID levelId) {
        return this.loadedLevels.get(levelId);
    }

    @NonNull public List<String> getValidLevelNames(@NonNull String levelNamePrefix) {
        return this.availableLevelIdsByName.keySet().stream()
                .filter(level -> level.toLowerCase().startsWith(levelNamePrefix))
                .collect(Collectors.toList());
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
        if (!gameRulesSupport) {
            //noinspection deprecation
            world.setGameRuleValue(name, String.valueOf(newValue));
            return;
        }
        GameRule<?> byName = GameRule.getByName(name);
        if (byName == null) return;
        //noinspection unchecked
        world.setGameRule((GameRule<Boolean>) byName, newValue);
    }

    private void setIntegerGameRule(@NonNull World world, @NonNull String name, int newValue) {
        if (!gameRulesSupport) {
            //noinspection deprecation
            world.setGameRuleValue(name, String.valueOf(newValue));
            return;
        }
        GameRule<?> byName = GameRule.getByName(name);
        if (byName == null) return;
        //noinspection unchecked
        world.setGameRule((GameRule<Integer>) byName, newValue);
    }

    @Nullable public UUID findLevelIdByName(@NonNull String levelName) {
        return this.availableLevelIdsByName.get(levelName);
    }
}

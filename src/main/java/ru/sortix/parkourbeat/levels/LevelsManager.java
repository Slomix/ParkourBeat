package ru.sortix.parkourbeat.levels;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.boss.DragonBattle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.inventory.type.CreateLevelMenu;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.levels.dao.files.FileLevelSettingDAO;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.lifecycle.PluginManager;
import ru.sortix.parkourbeat.utils.StringUtils;
import ru.sortix.parkourbeat.utils.java.ClassUtils;
import ru.sortix.parkourbeat.world.WorldsManager;

public class LevelsManager implements PluginManager {
    @Getter
    private final ParkourBeat plugin;

    private final WorldsManager worldsManager;
    private final File defaultLevelDirectory;

    @Getter
    private final LevelSettingsManager levelsSettings;

    private final AvailableLevelsCollection availableLevels;
    private final Map<UUID, Level> loadedLevelsById = new HashMap<>();
    private final Map<World, Level> loadedLevelsByWorld = new HashMap<>();
    private final boolean gameRulesSupport = ClassUtils.isClassPresent("org.bukkit.GameRule");
    private int nextLevelNumber = 1;

    public LevelsManager(@NonNull ParkourBeat plugin) {
        this.plugin = plugin;
        this.worldsManager = plugin.get(WorldsManager.class);
        this.defaultLevelDirectory = new File(this.plugin.getDataFolder(), "pb_default_level");
        if (!this.defaultLevelDirectory.isDirectory()) {
            throw new IllegalStateException(
                    "Default level directory not found: " + this.defaultLevelDirectory.getAbsolutePath());
        }
        this.levelsSettings = new LevelSettingsManager(new FileLevelSettingDAO(this));
        this.availableLevels = new AvailableLevelsCollection(this.plugin.getLogger());
        this.loadAvailableLevelNames();
    }

    private void loadAvailableLevelNames() {
        for (GameSettings gameSettings :
                this.levelsSettings.getLevelSettingDAO().loadAllAvailableLevelGameSettingsSync()) {
            this.availableLevels.add(gameSettings);
        }
        for (GameSettings gameSettings : this.availableLevels) {
            if (this.nextLevelNumber <= gameSettings.getUniqueNumber()) {
                this.nextLevelNumber = gameSettings.getUniqueNumber() + 1;
            }
        }
    }

    @NonNull public Collection<GameSettings> getAvailableLevelsSettings() {
        return Collections.unmodifiableCollection(Lists.newArrayList(this.availableLevels.iterator()));
    }

    @NonNull public CompletableFuture<Level> createLevel(
            @NonNull World.Environment environment, @NonNull UUID ownerId, @NonNull String ownerName) {
        CompletableFuture<Level> result = new CompletableFuture<>();
        UUID levelId = this.getNextLevelId();
        WorldCreator worldCreator = this.levelsSettings.getLevelSettingDAO().newWorldCreator(levelId);
        worldCreator.generator(this.worldsManager.getEmptyGenerator());
        worldCreator.environment(environment);

        if (!this.defaultLevelDirectory.isDirectory()) {
            this.plugin
                    .getLogger()
                    .severe("Default level directory not found: " + this.defaultLevelDirectory.getAbsolutePath());
            result.complete(null);
            return result;
        }

        this.worldsManager
                .createWorldFromCustomDirectory(worldCreator, this.defaultLevelDirectory)
                .thenAccept(world -> {
                    if (world == null) {
                        result.complete(null);
                        return;
                    }
                    try {
                        this.prepareLevelWorld(world, true);

                        int uniqueNumber = this.nextLevelNumber++;
                        String displayName = "Уровень #" + uniqueNumber;
                        LevelSettings levelSettings = LevelSettings.create(
                            world, environment, levelId, uniqueNumber, displayName, ownerId, ownerName);
                        world.setSpawnLocation(levelSettings.getWorldSettings().getSpawn());
                        Level level = new Level(levelSettings, world);
                        level.setEditing(true);

                        this.availableLevels.add(level.getLevelSettings().getGameSettings());
                        this.levelsSettings.addLevelSettings(levelId, levelSettings);
                        this.loadedLevelsById.put(levelId, level);
                        this.loadedLevelsByWorld.put(world, level);
                        result.complete(level);
                    } catch (Exception e) {
                        this.plugin.getLogger().log(java.util.logging.Level.SEVERE,
                            "Unable to create level", e);
                        result.complete(null);
                    }
                });
        return result;
    }

    @NonNull private UUID getNextLevelId() {
        UUID result;
        do {
            result = UUID.randomUUID();
        } while (this.availableLevels.byUniqueId(result) != null);
        return result;
    }

    @NonNull public CompletableFuture<Level> loadLevel(@NonNull UUID levelId, @Nullable GameSettings gameSettings) {
        CompletableFuture<Level> result = new CompletableFuture<>();

        Level level = getLoadedLevel(levelId);
        if (level != null) {
            result.complete(level);
            return result;
        }

        WorldCreator worldCreator = this.levelsSettings.getLevelSettingDAO().newWorldCreator(levelId);
        worldCreator.generator(this.worldsManager.getEmptyGenerator());
        worldCreator.environment(World.Environment.NORMAL); // TODO Load from settings
        this.worldsManager
                .createWorldFromDefaultContainer(worldCreator, this.worldsManager.getSyncExecutor())
                .thenAccept(world -> {
                    if (world == null) {
                        result.complete(null);
                        return;
                    }
                    try {
                        this.prepareLevelWorld(world, false);

                        LevelSettings levelSettings = this.levelsSettings.loadLevelSettings(levelId, gameSettings);
                        Level loadedLevel = new Level(levelSettings, world);
                        this.loadedLevelsById.put(levelId, loadedLevel);
                        this.loadedLevelsByWorld.put(world, loadedLevel);

                        result.complete(loadedLevel);
                    } catch (Exception e) {
                        this.plugin
                                .getLogger()
                                .log(java.util.logging.Level.SEVERE, "Не удалось загрузить уровень " + levelId, e);
                        result.complete(null);
                    }
                });
        return result;
    }

    @NonNull public CompletableFuture<Boolean> deleteLevelAsync(@NonNull GameSettings settings) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        UUID levelId = settings.getUniqueId();
        this.unloadLevelAsync(levelId).thenAccept(success -> {
            if (!success) {
                result.complete(false);
                return;
            }

            this.availableLevels.remove(settings);
            this.levelsSettings.getLevelSettingDAO().deleteLevelWorldAndSettings(levelId);
            result.complete(true);
        });
        return result;
    }

    @NonNull public CompletableFuture<Boolean> unloadLevelAsync(@NonNull UUID levelId) {
        if (!this.loadedLevelsById.containsKey(levelId)) return CompletableFuture.completedFuture(true);

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        LevelSettingDAO dao = this.levelsSettings.getLevelSettingDAO();

        CompletableFuture<Boolean> worldUnloading;
        World world = dao.getBukkitWorld(levelId);
        if (world == null) {
            worldUnloading = CompletableFuture.completedFuture(true);
        } else {
            worldUnloading = new CompletableFuture<>();
            this.plugin
                    .get(WorldsManager.class)
                    .unloadBukkitWorld(world, false, Settings.getLobbySpawn())
                    .thenAccept(worldUnloading::complete);
        }

        worldUnloading.thenAccept(success -> {
            if (!success) {
                result.complete(false);
                return;
            }
            this.levelsSettings.unloadLevelSettings(levelId);
            this.loadedLevelsById.remove(levelId);
            this.loadedLevelsByWorld.remove(world);
            result.complete(true);
        });

        return result;
    }

    @NonNull public CompletableFuture<Boolean> upgradeDataAsync(
            @NonNull UUID levelId, @Nullable Consumer<LevelSettings> updater) {
        boolean unload = this.getLoadedLevel(levelId) == null;
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        this.loadLevel(levelId, null).thenAccept(level -> {
            LevelSettings settings;
            try {
                settings = this.levelsSettings.loadLevelSettings(
                        levelId, level.getLevelSettings().getGameSettings());
            } catch (Exception e) {
                this.plugin
                        .getLogger()
                        .log(
                                java.util.logging.Level.SEVERE,
                                "Не удалось загрузить данные уровня " + levelId + " для конвертации",
                                e);
                result.complete(false);
                return;
            }
            boolean success = true;
            if (updater != null) {
                try {
                    updater.accept(settings);
                } catch (Exception e) {
                    this.plugin
                            .getLogger()
                            .log(
                                    java.util.logging.Level.SEVERE,
                                    "Не удалось произвести конвертацию уровня " + levelId,
                                    e);
                    success = false;
                }
            }
            try {
                this.levelsSettings.saveWorldSettings(levelId);
            } catch (Exception e) {
                this.plugin
                        .getLogger()
                        .log(
                                java.util.logging.Level.SEVERE,
                                "Не удалось сохранить данные уровня " + levelId + " после конвертации",
                                e);
                success = false;
            }
            if (unload) {
                boolean finalSuccess = success;
                this.unloadLevelAsync(levelId).thenAccept(success2 -> result.complete(finalSuccess && success2));
            } else {
                result.complete(success);
            }
        });
        return result;
    }

    public void saveLevelSettingsAndBlocks(@NonNull Level level) {
        this.levelsSettings.saveWorldSettings(level.getUniqueId());
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
    }

    @Nullable public Level getLoadedLevel(@NonNull UUID levelId) {
        return this.loadedLevelsById.get(levelId);
    }

    @Nullable public Level getLoadedLevel(@NonNull World world) {
        return this.loadedLevelsByWorld.get(world);
    }

    @NonNull public List<String> getUniqueLevelNames(@NonNull String levelNamePrefix, @Nullable CommandSender owner) {
        levelNamePrefix = levelNamePrefix.toLowerCase();

        List<String> result = new ArrayList<>();

        String uniqueName;
        if (owner == null) {
            for (GameSettings gameSettings : this.availableLevels.withUniqueNames()) {
                uniqueName = gameSettings.getUniqueName();
                if (uniqueName == null || !uniqueName.startsWith(levelNamePrefix)) continue;
                result.add(uniqueName);
            }
        } else {
            for (GameSettings gameSettings : this.availableLevels.withUniqueNames()) {
                if (!gameSettings.isOwner(owner, false, false)) continue;
                uniqueName = gameSettings.getUniqueName();
                if (uniqueName == null || !uniqueName.startsWith(levelNamePrefix)) continue;
                result.add(uniqueName);
            }
        }

        return result;
    }

    public void prepareLevelWorld(@NonNull World world, boolean updateGameRules) {
        world.setKeepSpawnInMemory(false);
        world.setAutoSave(false);

        if (CreateLevelMenu.DISPLAY_NON_DEFAULT_WORLD_TYPES) {
            DragonBattle battle = world.getEnderDragonBattle();
            if (battle != null) {
                EnderDragon dragon = battle.getEnderDragon();
                if (dragon != null) dragon.remove();
                battle.getBossBar().removeAll();
                battle.setRespawnPhase(DragonBattle.RespawnPhase.NONE);
            }
        }

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
        setBooleanGameRule(world, "REDUCED_DEBUG_INFO", false); // Should be switched to "true" after level publication
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

    @Nullable public GameSettings findLevel(@NonNull String levelUniqueNameOrIdOrNumber) {
        UUID levelId = StringUtils.parseUUID(levelUniqueNameOrIdOrNumber);
        if (levelId != null) {
            return this.availableLevels.byUniqueId(levelId);
        }
        try {
            return this.availableLevels.byUniqueNumber(Integer.parseInt(levelUniqueNameOrIdOrNumber));
        } catch (NumberFormatException e) {
            return this.availableLevels.byUniqueName(levelUniqueNameOrIdOrNumber);
        }
    }

    @Override
    public void disable() {
        for (Level level : this.loadedLevelsById.values()) {
            if (!level.isEditing()) continue;
            this.saveLevelSettingsAndBlocks(level);
        }
    }
}

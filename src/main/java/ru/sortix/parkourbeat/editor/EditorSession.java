package ru.sortix.parkourbeat.editor;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.editor.items.EditorItem;
import ru.sortix.parkourbeat.editor.items.ItemsContainer;
import ru.sortix.parkourbeat.editor.menu.SongMenu;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.ParticleController;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class EditorSession {

    private final Player owner;

    @Getter
    private final Level level;

    private final LevelsManager levelsManager;
    private final ItemsContainer editorItems;
    private final GameManager gameManager;
    private final Inventory songMenu;

    public EditorSession(
            @NonNull Player owner,
            @NonNull Level level,
            @NonNull LevelsManager levelsManager,
            @NonNull GameManager gameManager,
            @NonNull LevelEditorsManager levelEditorsManager) {
        this.owner = owner;
        this.level = level;
        this.levelsManager = levelsManager;
        this.gameManager = gameManager;
        this.editorItems = new ItemsContainer(owner, level, gameManager, levelEditorsManager);
        this.songMenu = new SongMenu(
                        ParkourBeat.getSongs(), owner, level.getLevelSettings().getGameSettings())
                .getInventory();
    }

    public void openSongMenu() {
        owner.openInventory(songMenu);
    }

    public <T extends EditorItem> T getEditorItem(Class<T> editorItemClass) {
        return editorItems.getEditorItem(editorItemClass);
    }

    public void start() {
        PlayerInventory inventory = owner.getInventory();
        inventory.clear();
        editorItems.giveToPlayer();

        LevelSettings levelSettings = level.getLevelSettings();
        WorldSettings worldSettings = levelSettings.getWorldSettings();
        ParticleController particleController = levelSettings.getParticleController();

        owner.setGameMode(GameMode.CREATIVE);
        owner.setFlying(true);
        TeleportUtils.teleport(this.owner, worldSettings.getSpawn());
        owner.sendMessage("Редактор уровня \"" + level.getLevelName() + "\" успешно запущен");

        particleController.loadParticleLocations(worldSettings.getWaypoints());
        particleController.startSpawnParticles(owner);

        level.setEditing(true);
    }

    public void stop() {
        level.getLevelSettings().getParticleController().stopSpawnParticles();
        level.setEditing(false);

        gameManager.removeGame(owner, false);

        owner.setGameMode(GameMode.ADVENTURE);
        owner.getInventory().clear();
        TeleportUtils.teleport(owner, Settings.getLobbySpawn());
        owner.sendMessage("Редактор уровня \"" + level.getLevelName() + "\" успешно остановлен");

        levelsManager.saveLevel(level);
        levelsManager.unloadLevel(level.getLevelId());
    }

    public static final int INTERACT_BLOCK_DISTANCE = 5;

    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        EditorItem editorItem = editorItems.getEditorItems().get(e.getItem());
        if (editorItem != null) {
            Location interactionPoint = e.getInteractionPoint();

            if (interactionPoint == null) {
                Player player = e.getPlayer();
                Location eyeLocation = player.getEyeLocation();
                Vector direction = eyeLocation.getDirection();
                RayTraceResult rayTrace =
                        player.getWorld().rayTraceBlocks(eyeLocation, direction, INTERACT_BLOCK_DISTANCE);
                if (rayTrace != null) {
                    interactionPoint = rayTrace.getHitPosition().toLocation(player.getWorld());
                }
            }

            editorItem.onClick(e.getAction(), e.getClickedBlock(), interactionPoint);
            e.setCancelled(true);
        }
    }
}

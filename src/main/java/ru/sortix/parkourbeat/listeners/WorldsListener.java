package ru.sortix.parkourbeat.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;

@RequiredArgsConstructor
public class WorldsListener implements Listener {
    public static int CHUNKS_LOADED = 0;

    private final LevelsManager levelsManager;

    @EventHandler
    private void on(WorldInitEvent event) {
        LevelSettingDAO levelSettingDAO = this.levelsManager.getLevelsSettings().getLevelSettingDAO();
        if (!levelSettingDAO.isLevelWorld(event.getWorld())) return;
        this.levelsManager.prepareLevelWorld(event.getWorld(), false);
    }

    @EventHandler
    private void on(ChunkLoadEvent event) {
        CHUNKS_LOADED++;
    }
}

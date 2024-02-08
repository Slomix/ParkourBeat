package ru.sortix.parkourbeat.editor;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.LevelsManager;

import java.util.HashMap;
import java.util.Map;

public class LevelEditorsManager {

    private final Map<Player, EditorSession> editorSessions = new HashMap<>();
    private final GameManager gameManager;
    private final LevelsManager levelsManager;

    public LevelEditorsManager(GameManager gameManager, LevelsManager levelsManager) {
        this.gameManager = gameManager;
        this.levelsManager = levelsManager;
    }

    public EditorSession createEditorSession(Player player, String levelName) {
        EditorSession editorSession = new EditorSession(player, levelName, levelsManager, gameManager);
        editorSessions.put(player, editorSession);
        return editorSession;
    }

    public void removeEditorSession(Player player) {
        EditorSession editorSession = editorSessions.remove(player);
        if (editorSession != null) {
            editorSession.stop();
        }
    }

    public EditorSession getEditorSession(Player player) {
        return editorSessions.get(player);
    }

    public void onPlayerInteract(PlayerInteractEvent e) {
        EditorSession editorSession = getEditorSession(e.getPlayer());
        if (editorSession != null) {
            editorSession.onPlayerInteract(e);
        }
    }
}

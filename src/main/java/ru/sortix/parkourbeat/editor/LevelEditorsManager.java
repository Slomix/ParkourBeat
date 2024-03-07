package ru.sortix.parkourbeat.editor;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class LevelEditorsManager {

    private final Map<Player, EditorSession> editorSessions = new HashMap<>();
    private final GameManager gameManager;

    @Getter
    private final LevelsManager levelsManager;

    public LevelEditorsManager(@NonNull GameManager gameManager, @NonNull LevelsManager levelsManager) {
        this.gameManager = gameManager;
        this.levelsManager = levelsManager;
    }

    public EditorSession createEditorSession(@NonNull Player player, @NonNull Level level) {
        EditorSession editorSession = new EditorSession(player, level, levelsManager, gameManager, this);
        editorSessions.put(player, editorSession);
        return editorSession;
    }

    public boolean removeEditorSession(Player player) {
        EditorSession editorSession = this.editorSessions.remove(player);
        if (editorSession == null) return false;

        editorSession.stop();
        return true;
    }

    @Nullable public EditorSession getEditorSession(Player player) {
        return editorSessions.get(player);
    }

    public void onPlayerInteract(PlayerInteractEvent e) {
        EditorSession editorSession = getEditorSession(e.getPlayer());
        if (editorSession != null) {
            editorSession.onPlayerInteract(e);
        }
    }
}

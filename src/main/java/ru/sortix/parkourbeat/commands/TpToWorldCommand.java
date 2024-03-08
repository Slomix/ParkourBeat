package ru.sortix.parkourbeat.commands;

import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class TpToWorldCommand implements CommandExecutor, TabCompleter {

    private final LevelsManager levelsManager;
    private final GameManager gameManager;
    private final LevelEditorsManager levelEditorsManager;

    public TpToWorldCommand(
            LevelEditorsManager levelEditorsManager, GameManager gameManager, LevelsManager levelsManager) {
        this.levelsManager = levelsManager;
        this.gameManager = gameManager;
        this.levelEditorsManager = levelEditorsManager;
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                String levelName = String.join(" ", args);
                UUID levelId = this.levelsManager.findLevelIdByName(levelName);
                if (levelId == null) {
                    sender.sendMessage("Уровень \"" + levelName + "\" не найден!");
                    return true;
                }

                this.levelsManager.loadLevel(levelId).thenAccept(level -> {
                    if (level == null) {
                        sender.sendMessage("Не удалось загрузить данные уровня");
                        return;
                    }
                    if (level.getWorld() == player.getWorld()) {
                        sender.sendMessage("You are already in this world!");
                        return;
                    }

                    TeleportUtils.teleport(
                            player, level.getLevelSettings().getWorldSettings().getSpawn());
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage("Вы телепортированы на уровень \"" + level.getLevelName() + "\"");
                });
            } else {
                TeleportUtils.teleport(player, Settings.getLobbySpawn());
                player.setGameMode(GameMode.ADVENTURE);
            }
            if (!levelEditorsManager.removeEditorSession(player)) {
                gameManager.removeGame(player);
            }
        } else {
            sender.sendMessage("Command only for players!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!(sender instanceof Player)) return null;
        if (args.length == 0) return null;
        return this.levelsManager.getValidLevelNames(String.join(" ", args).toLowerCase());
    }
}

package ru.sortix.parkourbeat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class EditorCommand implements CommandExecutor {

    private final LevelEditorsManager levelEditorsManager;
    private final LevelsManager levelsManager;

    public EditorCommand(LevelEditorsManager levelEditorsManager, LevelsManager levelsManager) {
        this.levelEditorsManager = levelEditorsManager;
        this.levelsManager = levelsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!levelsManager.getAllLevels().contains(args[0])) {
            sender.sendMessage("Level not found!");
        } else {
            levelEditorsManager.createEditorSession((Player) sender, args[0]).start();
        }
        return true;
    }
}

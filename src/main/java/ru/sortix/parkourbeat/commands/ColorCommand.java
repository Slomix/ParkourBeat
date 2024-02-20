package ru.sortix.parkourbeat.commands;

import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.editor.EditorSession;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.editor.items.ParticleItem;

public class ColorCommand implements CommandExecutor {

    private final LevelEditorsManager levelEditorsManager;

    public ColorCommand(LevelEditorsManager levelEditorsManager) {
        this.levelEditorsManager = levelEditorsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда только для игроков!");
            return true;
        }
        Player player = (Player) sender;
        EditorSession editorSession = levelEditorsManager.getEditorSession(player);
        if (editorSession == null) {
            player.sendMessage("Вы не в режиме редактирования!");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("Используйте: /color <hex>");
            return true;
        }
        String hex = args[0].startsWith("#") ? args[0].substring(1) : args[0];
        try {
            int r = Integer.valueOf(hex.substring(0, 2), 16);
            int g = Integer.valueOf(hex.substring(2, 4), 16);
            int b = Integer.valueOf(hex.substring(4, 6), 16);
            Color color = Color.fromRGB(r, g, b);
            editorSession.getEditorItem(ParticleItem.class).setCurrentColor(color);
            player.sendMessage("Текущий цвет установлен на #" + hex);
        } catch (Exception e) {
            player.sendMessage("Ошибка. Пожалуйста, убедитесь, что вы ввели правильный hex-код.");
        }
        return true;
    }
}

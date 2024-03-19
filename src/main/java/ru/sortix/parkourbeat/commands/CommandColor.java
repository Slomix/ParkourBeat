package ru.sortix.parkourbeat.commands;

import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.activity.type.EditActivity;

public class CommandColor extends ParkourBeatCommand {
    public CommandColor(@NonNull ParkourBeat plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда только для игроков");
            return true;
        }
        Player player = (Player) sender;
        UserActivity activity = this.plugin.get(ActivityManager.class).getActivity(player);
        if (!(activity instanceof EditActivity)) {
            player.sendMessage("Вы не в режиме редактирования!");
            return true;
        }
        EditActivity editActivity = ((EditActivity) activity);
        if (args.length == 0) {
            player.sendMessage("Используйте: /" + label + " <hex>");
            return true;
        }
        String hex = args[0].startsWith("#") ? args[0].substring(1) : args[0];
        try {
            int r = Integer.valueOf(hex.substring(0, 2), 16);
            int g = Integer.valueOf(hex.substring(2, 4), 16);
            int b = Integer.valueOf(hex.substring(4, 6), 16);
            Color color = Color.fromRGB(r, g, b);
            editActivity.setCurrentColor(color);
            player.sendMessage("Текущий цвет установлен на #" + hex);
        } catch (Exception e) {
            player.sendMessage("Ошибка. Пожалуйста, убедитесь, что вы ввели правильный hex-код.");
        }
        return true;
    }
}

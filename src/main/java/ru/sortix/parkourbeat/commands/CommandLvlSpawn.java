package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.type.UserActivity;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;

@Command(name = "lvlspawn")
public class CommandLvlSpawn {

    private final ParkourBeat plugin;

    public CommandLvlSpawn(ParkourBeat plugin) {
        this.plugin = plugin;
    }

    @Execute
    @Permission("command.lvlspawn")
    public void onCommand(@Context Player player) {
        ActivityManager activityManager = plugin.get(ActivityManager.class);
        UserActivity activity = activityManager.getActivity(player);
        
        if (activity != null && activity instanceof EditActivity) {
            Level level = activity.getLevel();
            Location spawnLocation = level.getSpawn();
            player.teleport(spawnLocation);
            player.sendMessage("Вы телепортированы на спавн уровня");
        } else {
            player.sendMessage("Вы не в редакторе");
        }
    }
}

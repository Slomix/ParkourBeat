package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(
    name = "pluginreload",
    aliases = {"preload"}
)
public class CommandPluginReload {

    private final ParkourBeat plugin;

    public CommandPluginReload(ParkourBeat plugin) {
        this.plugin = plugin;
    }

    @Execute
    @Permission(COMMAND_PERMISSION + ".pluginreload")
    public void onCommand(@Context Player player) {
        plugin.onDisable();
        plugin.onEnable();
        player.sendMessage("§d§l| §fПлагин успешно перезагружен!");
    }
}

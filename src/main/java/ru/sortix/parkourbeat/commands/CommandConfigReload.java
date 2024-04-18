package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(
    name = "configreload",
    aliases = {"creload"}
)
public class CommandConfigReload {

    private final ParkourBeat plugin;

    public CommandConfigReload(ParkourBeat plugin) {
        this.plugin = plugin;
    }

    @Execute
    @Permission(COMMAND_PERMISSION + ".configreload")
    public void onCommand(@Context Player player) {
        plugin.reloadConfig();
        sender.sendMessage("§d§l| §fКонфигурация плагина была успешно перезагружена");
    }
}

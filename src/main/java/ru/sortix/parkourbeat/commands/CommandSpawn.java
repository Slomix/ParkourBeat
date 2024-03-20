package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.utils.TeleportUtils;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(
        name = "spawn",
        aliases = {"lobby", "hub"})
@RequiredArgsConstructor
public class CommandSpawn {

    private final ParkourBeat plugin;

    @Execute
    @Permission(COMMAND_PERMISSION + ".spawn")
    public void onCommand(@Context Player player) {
        this.plugin.get(ActivityManager.class).setActivity(player, null);
        TeleportUtils.teleportAsync(this.plugin, player, Settings.getLobbySpawn());
    }
}

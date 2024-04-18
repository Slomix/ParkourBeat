package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.world.TeleportUtils;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(
    name = "lvlspawn",
    aliases = {"lspawn", "levelspawn", "spawnlvl", "spawnlevel"})
@RequiredArgsConstructor
public class CommandLvlSpawn {

    private final ParkourBeat plugin;

    @Execute
    @Permission(COMMAND_PERMISSION + ".lvlspawn")
    public void onCommand(@Context Player player) {
        if (!plugin.isPlayerInEditActivity(player)) {
            player.sendMessage("Ошибка: Вы не в редакторе");
            return;
        }

        TeleportUtils.teleportAsync(this.plugin, player, plugin.getEditActivity(player).getLevel().getLevelSettings().getWorldSettings().getSpawn());
    }
}

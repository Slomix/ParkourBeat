package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.world.WorldsManager;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(
    name = "pbconfigreload",
    aliases = {"pbreload"}
)
public class CommandConfigReload {

    private final ParkourBeat plugin;
    private final WorldsManager worldsManager;
    private final LevelsManager levelsManager;

    public CommandConfigReload(ParkourBeat plugin, WorldsManager worldsManager, LevelsManager levelsManager) {
        this.plugin = plugin;
        this.worldsManager = worldsManager;
        this.levelsManager = levelsManager;
    }

    @Execute
    @Permission(COMMAND_PERMISSION + ".pbconfigreload")
    public void onCommand(@Context Player player) {
        plugin.reloadConfig();
        Settings.load(plugin, worldsManager, levelsManager);
        player.sendMessage(Component.text("Конфигурация плагина успешно перезагружена.", NamedTextColor.YELLOW));
    }
}

package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.player.music.MusicTracksManager;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(name = "updatetrack")
@RequiredArgsConstructor
public class CommandUpdateTrack {
    private final ParkourBeat plugin;

    @Execute
    @Permission(COMMAND_PERMISSION + ".updatetrack")
    public void onCommand(@Context CommandSender sender, @Arg String... trackName) {
        this.plugin.get(MusicTracksManager.class).updateTrackFileInfo(String.join(" ", trackName));
    }
}

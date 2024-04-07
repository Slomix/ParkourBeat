package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import ru.sortix.parkourbeat.ParkourBeat;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(name = "test")
@RequiredArgsConstructor
public class CommandTest {
    @SuppressWarnings("unused")
    private final ParkourBeat plugin;

    @Execute
    @Permission(COMMAND_PERMISSION + ".test")
    public String onCommand() {
        return "Nothing here";
    }
}

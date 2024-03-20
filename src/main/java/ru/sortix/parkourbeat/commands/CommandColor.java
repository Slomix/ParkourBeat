package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.constant.Messages;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(name = "color")
@RequiredArgsConstructor
public class CommandColor {

    private final ParkourBeat plugin;

    @Execute
    @Permission(COMMAND_PERMISSION + ".color")
    public String onExecute(@Context Player sender, @Arg("hex") Color color) {
        UserActivity activity = this.plugin.get(ActivityManager.class).getActivity(sender);
        if (!(activity instanceof EditActivity)) {
            return Messages.NOT_IN_EDIT_MODE;
        }

        EditActivity editActivity = ((EditActivity) activity);

        editActivity.setCurrentColor(color);
        return String.format(Messages.COLOR_SET, color.asRGB());
    }
}

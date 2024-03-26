package ru.sortix.parkourbeat.commands.handler;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invalidusage.InvalidUsageHandler;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.schematic.Schematic;
import org.bukkit.command.CommandSender;
import ru.sortix.parkourbeat.constant.Messages;

public class DefaultInvalidUsageHandler implements InvalidUsageHandler<CommandSender> {

    @Override
    public void handle(
        Invocation<CommandSender> invocation,
        InvalidUsage<CommandSender> commandSenderInvalidUsage,
        ResultHandlerChain<CommandSender> resultHandlerChain) {
        CommandSender sender = invocation.sender();
        Schematic schematic = commandSenderInvalidUsage.getSchematic();

        if (schematic.isOnlyFirst()) {
            sender.sendMessage(String.format("%s: %s", Messages.COMMAND_USAGE, schematic.first()));
            return;
        }

        sender.sendMessage(String.format("%s", Messages.COMMAND_USAGE));
        for (String scheme : schematic.all()) {
            sender.sendMessage(String.format(" - %s", scheme));
        }
    }
}

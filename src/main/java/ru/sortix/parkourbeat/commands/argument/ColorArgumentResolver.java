package ru.sortix.parkourbeat.commands.argument;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;

public class ColorArgumentResolver extends ArgumentResolver<CommandSender, Color> {

    @Override
    protected ParseResult<Color> parse(Invocation<CommandSender> invocation, Argument<Color> context, String argument) {
        String hex = argument.startsWith("#") ? argument.substring(1) : argument;

        try {
            int r = Integer.valueOf(hex.substring(0, 2), 16);
            int g = Integer.valueOf(hex.substring(2, 4), 16);
            int b = Integer.valueOf(hex.substring(4, 6), 16);
            return ParseResult.success(Color.fromRGB(r, g, b));
        } catch (Exception e) {
            return ParseResult.failure("Ошибка. Пожалуйста, убедитесь, что вы ввели правильный hex-код.");
        }
    }
}

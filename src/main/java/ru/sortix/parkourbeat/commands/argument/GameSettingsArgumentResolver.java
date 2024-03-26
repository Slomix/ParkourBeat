package ru.sortix.parkourbeat.commands.argument;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

@RequiredArgsConstructor
public class GameSettingsArgumentResolver extends ArgumentResolver<CommandSender, GameSettings> {
    private final LevelsManager levelsManager;
    private final boolean allowPlayers;
    private final boolean allowConsole;
    private final boolean onlyOwning;

    @Override
    protected ParseResult<GameSettings> parse(
        Invocation<CommandSender> invocation, Argument<GameSettings> context, String argument) {
        GameSettings gameSettings = this.levelsManager.findLevel(argument);

        if (gameSettings == null) {
            return ParseResult.failure(String.format("Уровень \"%s\" не найден!", argument));
        }
        return ParseResult.success(gameSettings);
    }

    @Override
    public SuggestionResult suggest(
        Invocation<CommandSender> invocation, Argument<GameSettings> argument, SuggestionContext context) {
        CommandSender sender = invocation.sender();
        if (sender instanceof Player) {
            if (!this.allowPlayers) return SuggestionResult.empty();
        } else {
            if (!this.allowConsole) return SuggestionResult.empty();
        }
        return SuggestionResult.of(
            this.levelsManager.getUniqueLevelNames(
                context.getCurrent().multilevel(),
                this.onlyOwning ? sender : null,
                !(sender instanceof Player)
            )
        );
    }
}

package ru.sortix.parkourbeat.activity.type;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.*;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.game.movement.GameMoveHandler;
import ru.sortix.parkourbeat.item.ItemsManager;
import ru.sortix.parkourbeat.item.editor.type.TestGameItem;

public class PlayActivity extends UserActivity {
    @NonNull public static CompletableFuture<PlayActivity> createAsync(
            @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull UUID levelId, boolean isEditorGame) {
        UserActivity activity = plugin.get(ActivityManager.class).getActivity(player);
        if (activity instanceof PlayActivity
                && activity.getLevel().getUniqueId().equals(levelId)
                && ((PlayActivity) activity).isEditorGame == isEditorGame) {
            return CompletableFuture.completedFuture((PlayActivity) activity);
        }

        CompletableFuture<PlayActivity> result = new CompletableFuture<>();
        Game.createAsync(plugin, player, levelId).thenAccept(game -> {
            if (game == null) {
                result.complete(null);
                return;
            }
            result.complete(new PlayActivity(game, isEditorGame));
        });
        return result;
    }

    private final @NonNull Game game;
    private final boolean isEditorGame;

    private PlayActivity(@NonNull Game game, boolean isEditorGame) {
        super(game.getPlugin(), game.getPlayer(), game.getLevel());
        this.game = game;
        this.isEditorGame = isEditorGame;
    }

    @Override
    public void startActivity() {
        this.game.failLevel("§cВы умерли");

        this.player.setGameMode(GameMode.ADVENTURE);

        this.player.getInventory().clear();
        if (this.isEditorGame) {
            this.plugin.get(ItemsManager.class).putItem(this.player, TestGameItem.class);
        }
    }

    @Override
    public void on(@NonNull PlayerResourcePackStatusEvent event) {
        switch (event.getStatus()) {
            case ACCEPTED: {
                this.player.sendMessage(
                        "Началась загрузка мелодии. " + " После окончания загрузки вы сможете начать игру");
                return;
            }
            case FAILED_DOWNLOAD: {
                this.player.sendMessage("Ошибка загрузки мелодии."
                        + " Вам доступна игра без ресурс-пака, "
                        + "однако мы рекомендуем всё же установить пакет ресурсов для более комфортной игры");
                this.game.setCurrentState(Game.State.READY);
                return;
            }
            case DECLINED: {
                this.player.sendMessage("Вы отказались от загрузки мелодии. "
                        + "Если вы захотите вновь загрузить ресурс-пак - убедитесь, "
                        + "что в настройках сервера у вас разрешено принятие пакетов ресурсов");
                this.game.setCurrentState(Game.State.READY);
                return;
            }
            case SUCCESSFULLY_LOADED: {
                this.player.sendMessage("Мелодия успешно загружена, приятной игры!");
                this.game.setCurrentState(Game.State.READY);
                return;
            }
            default: {
                throw new IllegalArgumentException("Unknown status: " + event.getStatus());
            }
        }
    }

    @Override
    public void on(@NonNull PlayerMoveEvent event) {
        Game.State state = this.game.getCurrentState();
        GameMoveHandler gameMoveHandler = this.game.getGameMoveHandler();
        if (state == Game.State.PREPARING) {
            gameMoveHandler.onPreparingState(event);
        } else if (state == Game.State.READY) {
            gameMoveHandler.onReadyState(this.player);
        } else if (state == Game.State.RUNNING) {
            gameMoveHandler.onRunningState(this.player, event.getFrom(), event.getTo());
        }
    }

    @Override
    public void onTick() {}

    @Override
    public void on(@NonNull PlayerToggleSprintEvent event) {
        if (this.game.getCurrentState() == Game.State.RUNNING) {
            this.game.getGameMoveHandler().onRunningState(event);
        }
    }

    @Override
    public void on(@NonNull PlayerToggleSneakEvent event) {
        if (event.isSneaking() && this.game.getCurrentState() == Game.State.RUNNING) {
            this.game.failLevel("§cВы остановились");
        }
    }

    @Override
    public void on(@NonNull PlayerInteractEvent event) {
        event.setUseInteractedBlock(Event.Result.DENY);
    }

    @Override
    public int getFallHeight() {
        return this.getFallHeight(false);
    }

    @Override
    public void onPlayerFall() {
        this.game.failLevel("§cВы упали");
    }

    @Override
    public void endActivity() {
        this.game.failLevel("§cВы умерли");
        this.game.endGame();
    }
}

package ru.sortix.parkourbeat.game;

import me.bomb.amusic.AMusic;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;

public class Game {

    private Player player;
    private LevelSettings levelSettings;
    private State currentState;
    private GameMoveHandler gameMoveHandler;

    public void prepare(Player player, String levelName) {
        currentState = State.PREPARING;
        this.player = player;

        World world = ParkourBeat.getLevelsManager().loadLevel(levelName);
        levelSettings = ParkourBeat.getLevelsManager().loadLevelSettings(world);
        WorldSettings worldSettings = levelSettings.getWorldSettings();
        GameSettings gameSettings = levelSettings.getGameSettings();

        player.teleport(worldSettings.getSpawn());
        this.gameMoveHandler = new GameMoveHandler(this);

        if (!gameSettings.getSongPlayListName().equals(AMusic.getPackName(player))) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(ParkourBeat.getInstance(), () -> {
                AMusic.loadPack(player, gameSettings.getSongPlayListName(), false);
            }, 20L);

        }


    }

    public void start() {
        if (currentState != State.READY) {
            return;
        }
        GameSettings gameSettings = levelSettings.getGameSettings();
        player.sendMessage("Game start!");

        AMusic.setRepeatMode(player, null);
        AMusic.playSound(player, gameSettings.getSongName());
        currentState = State.RUNNING;
    }

    @NotNull
    public LevelSettings getLevelSettings() {
        return levelSettings;
    }

    @NotNull
    public GameMoveHandler getGameMoveHandler() {
        return gameMoveHandler;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public void stopGame(StopReason reason) {
        player.teleport(levelSettings.getWorldSettings().getSpawn());
        player.setGameMode(GameMode.ADVENTURE);
        if (reason == StopReason.WRONG_DIRECTION) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cНельзя бежать назад"));
        } else if (reason == StopReason.LOOSE) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cВы проиграли"));
        } else if (reason == StopReason.FINISH) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§aВы прошли уровень"));
        }
        AMusic.stopSound(player);
        currentState = State.READY;
    }

    public void endGame() {
        player.teleport(Settings.getExitLocation());
        World world = levelSettings.getWorldSettings().getWorld();
        if (world.getPlayers().isEmpty())
            ParkourBeat.getLevelsManager().unloadLevel(world.getName());
    }

    public enum State {
        PREPARING,
        READY,
        RUNNING,
    }

    public enum StopReason {
        FINISH,
        LOOSE,
        WRONG_DIRECTION,
        LEAVE
    }

}

package ru.sortix.parkourbeat.player.scoreboard;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;

class BoardLine {
    protected final BasicScoreboard board;
    protected final Team team;
    protected final String scoreEntry;

    public BoardLine(@NonNull BasicScoreboard board, @NonNull Team team) {
        this.board = board;
        this.team = team;
        this.scoreEntry = this.team.getEntries().iterator().next();
    }

    @Nullable
    public Component getText() {
        return this.team.prefix();
    }

    @NonNull
    public BoardLine setText(@Nullable Component text) {
        this.team.prefix(text);
        return this;
    }

    public void removeLine() {
        // this.board.objective.getScore(this.scoreEntry).resetScore(); // Paper 1.17+
        this.team.unregister();
        this.board.lines.remove(this);
    }
}

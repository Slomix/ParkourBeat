package ru.sortix.parkourbeat.player.scoreboard;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BasicScoreboard {
    private static final Scoreboard EMPTY_BOARD = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
    public static final byte MAX_LINES = 15;

    protected final Plugin plugin;
    protected final Server server;
    protected final UUID viewerId;
    protected final Scoreboard scoreboard;
    protected final Objective objective;

    final List<BoardLine> lines = new ArrayList<>();
    private int lastEntryNumber = 0;

    public BasicScoreboard(@NonNull Plugin plugin,
                           @NonNull Player viewer,
                           @NonNull Scoreboard scoreboard,
                           @NonNull Component title
    ) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.viewerId = viewer.getUniqueId();
        this.scoreboard = scoreboard;
        DisplaySlot slot = DisplaySlot.SIDEBAR;
        Objective objective = this.scoreboard.getObjective(viewer.getName() + "_" + slot.name());
        if (objective != null) objective.unregister();
        this.objective = this.scoreboard.registerNewObjective(
            UUID.randomUUID().toString().replace("-", "").substring(0, 16),
            "dummy",
            title,
            RenderType.INTEGER
        );
        this.objective.setDisplaySlot(slot);
    }

    @NonNull
    public Component getTitle() {
        return this.objective.displayName();
    }

    @NonNull
    public BasicScoreboard setTitle(@Nullable Component title) {
        this.objective.displayName(title);
        return this;
    }

    @NonNull
    private BasicScoreboard addLine(Component text) {
        if (this.lines.size() >= MAX_LINES) {
            throw new UnsupportedOperationException("Unable to add more than " + MAX_LINES + " lines");
        }

        StringBuilder entry = new StringBuilder();
        for (char ch : String.format("%04X", this.lastEntryNumber++).toLowerCase().toCharArray()) {
            entry.append('§').append(ch);
        }

        String teamName = "team" + (this.lines.size() + 1);
        Team team = this.scoreboard.getTeam(teamName);
        if (team == null) team = this.scoreboard.registerNewTeam(teamName);
        team.addEntry(entry.toString());

        BoardLine boardLine = new BoardLine(this, team);
        this.lines.add(boardLine);
        this.objective.getScore(boardLine.scoreEntry).setScore(0);

        if (text != null) boardLine.setText(text);

        return this;
    }

    @NonNull
    public BasicScoreboard setLines(@NonNull Component... lines) {
        return this.setLines(Arrays.asList(lines));
    }

    @NonNull
    public BasicScoreboard setLines(@NonNull List<Component> lines) {
        int amountDelta = this.lines.size() - lines.size();
        if (amountDelta > 0) {
            for (int i = 0; i < amountDelta; i++) {
                this.lines.get(this.lines.size() - 1).removeLine();
            }
        } else if (amountDelta < 0) {
            for (int i = 0; i < -amountDelta; i++) {
                this.addLine(null);
            }
        }
        for (int i = 0; i < lines.size(); i++) {
            this.lines.get(i).setText(lines.get(i));
        }
        return this;
    }

    @NonNull
    public BasicScoreboard show() {
        this.setViewerScoreboard(this.scoreboard);
        return this;
    }

    @NonNull
    public BasicScoreboard hide() {
        this.setViewerScoreboard(null);
        return this;
    }

    private void setViewerScoreboard(@Nullable Scoreboard scoreboard) {
        Player player = this.server.getPlayer(this.viewerId);
        if (player != null) {
            player.setScoreboard(scoreboard == null ? EMPTY_BOARD : scoreboard);
        }
    }
}

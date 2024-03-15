package ru.sortix.parkourbeat.levels;

import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

@RequiredArgsConstructor
public class Level {
    @Getter
    private final @NonNull UUID levelId;

    @Getter
    private final @NonNull String levelName;

    @Getter
    private final @NonNull World world;

    @Getter
    private final @NonNull LevelSettings levelSettings;

    private boolean isEditing = false;

    public boolean isEditing() {
        return this.isEditing;
    }

    public void setEditing(boolean isEditing) {
        this.isEditing = isEditing;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Level)) return false;
        return ((Level) other).levelId.equals(this.levelId);
    }
}

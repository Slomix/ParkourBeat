package ru.sortix.parkourbeat.listeners.physics;

import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.activity.type.SpectateActivity;

public class CustomPhysicsChecker {

    private final ActivityManager manager;

    public CustomPhysicsChecker(ParkourBeat plugin) {
        this.manager = plugin.get(ActivityManager.class);
    }

    public boolean getCustomPhysicsRule(Player player) {
        UserActivity activity = manager.getActivity(player);
        if (activity == null || activity instanceof SpectateActivity) return false;
        return activity.getLevel().getLevelSettings().getGameSettings().isCustomPhysicsEnabled();
    }

}

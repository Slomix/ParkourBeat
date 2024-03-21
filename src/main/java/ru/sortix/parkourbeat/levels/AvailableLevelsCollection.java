package ru.sortix.parkourbeat.levels;

import java.util.*;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

@RequiredArgsConstructor
public class AvailableLevelsCollection implements Iterable<GameSettings> {
    private final Logger logger;

    private final Set<String> ignoredNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, GameSettings> byUniqueName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Set<UUID> ignoredIds = new HashSet<>();
    private final Map<UUID, GameSettings> byUniqueId = new HashMap<>();
    private final Set<Integer> ignoredNumbers = new HashSet<>();
    private final Map<Integer, GameSettings> byUniqueNumber = new HashMap<>();

    public synchronized void add(@NonNull GameSettings settings) {
        String uniqueName = settings.getUniqueName();
        if (uniqueName != null) {
            if (this.ignoredNames.contains(uniqueName) || this.byUniqueName.put(uniqueName, settings) != null) {
                this.byUniqueName.remove(uniqueName);
                this.ignoredNames.add(uniqueName);
                this.logger.warning("Обнаружен дубликат уровня по уникальному названию: " + uniqueName);
            }
        }

        UUID uniqueId = settings.getUniqueId();
        if (this.ignoredIds.contains(uniqueId) || this.byUniqueId.put(uniqueId, settings) != null) {
            this.byUniqueId.remove(uniqueId);
            this.ignoredIds.add(uniqueId);
            this.logger.warning("Обнаружен дубликат уровня по уникальному айди: " + uniqueId);
        }

        int uniqueNumber = settings.getUniqueNumber();
        if (this.ignoredNumbers.contains(uniqueNumber) || this.byUniqueNumber.put(uniqueNumber, settings) != null) {
            this.byUniqueNumber.remove(uniqueNumber);
            this.ignoredNumbers.add(uniqueNumber);
            this.logger.warning("Обнаружен дубликат уровня по уникальному номеру: " + uniqueNumber);
        }
    }

    public synchronized void remove(@NonNull GameSettings settings) {
        String uniqueName = settings.getUniqueName();
        if (uniqueName != null) this.byUniqueName.remove(uniqueName);

        UUID uniqueId = settings.getUniqueId();
        this.byUniqueId.remove(uniqueId);

        int uniqueNumber = settings.getUniqueNumber();
        this.byUniqueNumber.remove(uniqueNumber);
    }

    @Nullable public GameSettings byUniqueName(@NonNull String uniqueName) {
        return this.byUniqueName.get(uniqueName);
    }

    @Nullable public GameSettings byUniqueId(@NonNull UUID uniqueId) {
        return this.byUniqueId.get(uniqueId);
    }

    @Nullable public GameSettings byUniqueNumber(int uniqueNumber) {
        return this.byUniqueNumber.get(uniqueNumber);
    }

    @NotNull @Override
    public Iterator<GameSettings> iterator() {
        return this.byUniqueId.values().iterator();
    }

    @NonNull public Collection<GameSettings> withUniqueNames() {
        return this.byUniqueId.values();
    }
}

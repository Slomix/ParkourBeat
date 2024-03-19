package ru.sortix.parkourbeat.utils;

import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;

public class StringUtils {
    private static final int UUID_LENGTH = "00000000-0000-0000-0000-000000000000".length();

    @Nullable public static UUID parseUUID(@NonNull String string) {
        if (string.length() != UUID_LENGTH || string.split("-").length != 5) {
            return null;
        }
        try {
            return UUID.fromString(string);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

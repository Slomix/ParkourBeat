package ru.sortix.parkourbeat.utils.java;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassUtils {
    public boolean isClassPresent(@NonNull String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

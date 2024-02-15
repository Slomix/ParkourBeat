package ru.sortix.parkourbeat.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class SkullTextures {

    private static Field profileField;

    public static SkullMeta setSkullTexture(SkullMeta headMeta, String texture) {
        if (profileField == null) {
            try {
                profileField = headMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", texture));
        try {
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }

        return headMeta;
    }


}

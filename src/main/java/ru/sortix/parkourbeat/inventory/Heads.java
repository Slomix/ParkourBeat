package ru.sortix.parkourbeat.inventory;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nullable;
import java.util.Base64;
import java.util.UUID;

@SuppressWarnings("unused")
public class Heads {
    private static final Material HEAD_MATERIAL = Material.PLAYER_HEAD;

    public static void cloneProfile(@NonNull ItemStack from, @NonNull ItemStack to) {
        ItemMeta fromMeta = from.getItemMeta();
        if (!(fromMeta instanceof SkullMeta)) throw new IllegalArgumentException();
        if (to.getType() != HEAD_MATERIAL) to.setType(HEAD_MATERIAL);
        SkullMeta toMeta = (SkullMeta) to.getItemMeta();
        toMeta.setPlayerProfile(((SkullMeta) fromMeta).getPlayerProfile());
        to.setItemMeta(toMeta);
    }

    @NonNull
    public static ItemStack getHeadByRawData(@NonNull String data) {
        data = replaceFirst(data, "http://");
        data = replaceFirst(data, "https://");
        data = replaceFirst(data, "textures.minecraft.net/texture/");
        if (data.length() <= 16) {
            return getHeadByLicenseName(data);
        } else if (data.length() <= 64) {
            return getHeadByHash(data);
        } else {
            return getHeadByTextureData(data, !data.startsWith("{"));
        }
    }

    @NonNull
    private static String replaceFirst(@NonNull String input, @NonNull String prefix) {
        if (!input.startsWith(prefix)) {
            return input;
        }
        return input.substring(prefix.length());
    }

    @NonNull
    public static ItemStack getHeadByLicenseName(@NonNull String name) {
        return createPlayerHeadWithProfile(Bukkit.createProfile(UUID.randomUUID(), name));
    }

    @NonNull
    public static ItemStack getHeadByGamer(@NonNull Player player) {
        return createPlayerHeadWithProfile(player.getPlayerProfile());
    }

    @NonNull
    public static ItemStack getHeadByHash(@NonNull String hash) {
        return getHeadByTextureData(
            "{\"textures\":{\"SKIN\":{\"url\":\"https://textures.minecraft.net/texture/" + hash + "\"}}}", false);
    }

    @NonNull
    public static ItemStack getHeadByTextureData(@NonNull String value, boolean base64) {
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "");
        if (!base64) {
            value = new String(Base64.getEncoder().encode(value.getBytes()));
        }
        profile.setProperty(new ProfileProperty("textures", value));
        return createPlayerHeadWithProfile(profile);
    }

    @NonNull
    public static ItemStack getHeadWithoutSkin() {
        return createPlayerHeadWithProfile(null);
    }

    @NonNull
    private static ItemStack createPlayerHeadWithProfile(@Nullable PlayerProfile profile) {
        ItemStack head = new ItemStack(HEAD_MATERIAL);
        if (profile != null) {
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            headMeta.setPlayerProfile(profile);
            head.setItemMeta(headMeta);
        }
        return head;
    }

    @Nullable
    public static String getProfileSkinHash(@NonNull PlayerProfile profile) {
        String url = getProfileSkinUrl(profile);
        return url == null ? null : url.substring(url.lastIndexOf("/") + 1);
    }

    @Nullable
    public static String getProfileSkinUrl(@NonNull PlayerProfile profile) {
        for (ProfileProperty property : profile.getProperties()) {
            if (property.getName().equals("textures")) {
                String json = new String(Base64.getDecoder().decode(property.getValue()));
                JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
                jsonObject = jsonObject.getAsJsonObject("textures").getAsJsonObject("SKIN");
                return jsonObject.get("url").getAsString();
            }
        }
        return null;
    }
}

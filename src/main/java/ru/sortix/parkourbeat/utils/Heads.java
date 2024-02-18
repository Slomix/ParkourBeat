package ru.sortix.parkourbeat.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Base64;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Heads {

  private static final boolean USE_MODERN_HEADS = Material.getMaterial("PLAYER_HEAD") != null;
  private static final Material HEAD_MATERIAL =
      Material.getMaterial(USE_MODERN_HEADS ? "PLAYER_HEAD" : "SKULL_ITEM");

  public static void cloneProfile(ItemStack from, ItemStack to) {
    ItemMeta fromMeta = from.getItemMeta();
    if (!(fromMeta instanceof SkullMeta)) throw new IllegalArgumentException();
    if (to.getType() != HEAD_MATERIAL) to.setType(HEAD_MATERIAL);
    SkullMeta toMeta = (SkullMeta) to.getItemMeta();
    toMeta.setPlayerProfile(((SkullMeta) fromMeta).getPlayerProfile());
    to.setItemMeta(toMeta);
  }

  public static ItemStack getHeadByRawData(String data) {
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

  private static String replaceFirst(String input, String prefix) {
    if (!input.startsWith(prefix)) {
      return input;
    }
    return input.substring(prefix.length());
  }

  public static ItemStack getHeadByLicenseName(String name) {
    return createPlayerHeadWithProfile(Bukkit.createProfile(UUID.randomUUID(), name));
  }

  public static ItemStack getHeadByGamer(Player player) {
    return createPlayerHeadWithProfile(player.getPlayerProfile());
  }

  public static ItemStack getHeadByHash(String hash) {
    return getHeadByTextureData(
        "{\"textures\":{\"SKIN\":{\"url\":\"https://textures.minecraft.net/texture/"
            + hash
            + "\"}}}",
        false);
  }

  public static ItemStack getHeadByTextureData(String value, boolean base64) {
    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "");
    if (!base64) {
      value = new String(Base64.getEncoder().encode(value.getBytes()));
    }
    profile.setProperty(new ProfileProperty("textures", value));
    return createPlayerHeadWithProfile(profile);
  }

  public static ItemStack getHeadWithoutSkin() {
    return createPlayerHeadWithProfile(null);
  }

  private static ItemStack createPlayerHeadWithProfile(@Nullable PlayerProfile profile) {
    ItemStack head;
    if (USE_MODERN_HEADS) {
      head = new ItemStack(HEAD_MATERIAL);
    } else {
      head = new ItemStack(HEAD_MATERIAL, 1, (short) 3);
    }
    if (profile != null) {
      SkullMeta headMeta = (SkullMeta) head.getItemMeta();
      headMeta.setPlayerProfile(profile);
      head.setItemMeta(headMeta);
    }
    return head;
  }

  @Nullable public static String getProfileSkinHash(PlayerProfile profile) {
    String url = getProfileSkinUrl(profile);
    return url == null ? null : url.substring(url.lastIndexOf("/") + 1);
  }

  @Nullable public static String getProfileSkinUrl(PlayerProfile profile) {
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

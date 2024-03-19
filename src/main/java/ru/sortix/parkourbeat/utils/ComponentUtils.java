package ru.sortix.parkourbeat.utils;

import javax.annotation.Nullable;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ComponentUtils {
    @NonNull public static BaseComponent createCopyTextComponent(
            @NonNull String displayText, @Nullable String hoverText, @NonNull String copyText) {
        BaseComponent result = new TextComponent(displayText);
        if (hoverText != null) {
            result.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
        }
        result.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText));
        return result;
    }
}

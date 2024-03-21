package ru.sortix.parkourbeat.player.input.type;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatInput implements PlayerInput {
    private final @NonNull String message;
}

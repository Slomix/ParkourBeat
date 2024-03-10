package ru.sortix.parkourbeat.levels.settings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Song {
    private @NonNull String songPlaylist;
    private @NonNull String songName;
}

package ru.sortix.parkourbeat.levels.settings;

public class GameSettings {

  private final String owner;
  private String songPlayListName;
  private String songName;

  public GameSettings(String songPlayListName, String songName, String owner) {
    this.songPlayListName = songPlayListName;
    this.songName = songName;
    this.owner = owner;
  }

  public String getSongPlayListName() {
    return songPlayListName;
  }

  public String getSongName() {
    return songName;
  }

  public String getOwner() {
    return owner;
  }

  public void setSong(String playlist, String name) {
    this.songPlayListName = playlist;
    this.songName = name;
  }
}

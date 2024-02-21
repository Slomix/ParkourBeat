### Project build:

- Clone project
- Run `mvn package`
- Output jar will be `ParkourBeat-1.0-SNAPSHOT.jar` in `target` dir

### Plugin dependencies:

- Paper 1.12.2 or 1.16.5 (other versions haven't been tested, but probably work too)
- [AMusic 0.12](https://spigotmc.org/resources/108835)

### Plugin installation:

- Make sure if AMusic is installed
- Create or copy world with name "pb_lobby" for player lobby (use MultiVerse for example)
- Create or copy world with name "pb_default_level" for all new use (using MultiVerse for example)
- Create world "pb_lobby" using [MultiVerse](https://spigotmc.org/resources/390) or simple copy world directory and name
  it "pb_lobby"
- Create world "pb_default_level" using [MultiVerse](https://spigotmc.org/resources/390) or simple copy world directory
  and name it "pb_default_level"
- Copy `ParkourBeat-1.0-SNAPSHOT.jar` to `plugins` folder
- Restart server to load plugin
- Edit all configs in `plugins/ParkourBeat` directory
- Restart server to apply new configs

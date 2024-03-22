# About plugin

### Plugin dependencies:

- Paper 1.12.2 or 1.16.5 (other versions haven't been tested, but probably work too)
- [AMusic 0.12](https://spigotmc.org/resources/108835)

### Plugin build:

- Clone project
- Run `./gradlew shadowRun`
- Output jar will be `build/libs/ParkourBeat-<version>-all.jar`

### Manual testing of plugin:

- Clone project
- Run `./gradlew runTask`
- Connect to server by the address `localhost`

### Plugin deployment:

- Make sure if AMusic is installed
- Create world "pb_lobby" using [MultiVerse](https://spigotmc.org/resources/390) or simple copy world directory and name
  it "pb_lobby"
- Create world "pb_default_level" using [MultiVerse](https://spigotmc.org/resources/390) or simple copy world directory
  and name it "pb_default_level"
- Build plugin
- Copy `ParkourBeat-<version>-all.jar` to `plugins` folder
- Restart server to load plugin
- Edit all configs in `plugins/ParkourBeat` directory
- Restart server to apply new configs

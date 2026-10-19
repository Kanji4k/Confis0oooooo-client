# Confis0oooooo Client

Fabric mod for Minecraft 1.21.11 (client only, needs Fabric API).

Modules: FPS, armor status, ping, waypoints, clock, day, server TPS, keystrokes, CPS, minimap.
Press **Right Shift** in game to open the menu (press again to close). Settings save to `config/confis0oooooo.json`.

## Build
- Locally: install JDK 21 + Gradle 9.3, then `gradle build`. Jar appears in `build/libs/`.
- Or push this folder to a GitHub repo; the included Actions workflow builds the jar (Actions tab -> artifact).
- If Gradle can't resolve a version, pick current ones for 1.21.11 from https://fabricmc.net/develop and edit `gradle.properties`.

# Cesium for Fabric
Cesium is a mod designed to improve the flaws of Minecraft’s storage format called Anvil.
It uses LMDB as its storage backend and utilises zstd for better chunk data compression

## ⚠️ Disclaimer
This mod is in an experimental state and could damage your world files. While there are no current known cases of this mod crashing, bugs can always arise. Please report all bugs to my issue tracker.

**Create a backup of your world periodically, even if you do not use this mod!**

## ❓ What it does
Cesium creates a database for player data and for each dimension respectively. Saved data is compressed using a more efficient compression algorithm to reduce the disk space needed for most world data.
To prevent out-of-sync player and world data Cesium flushes all data to disk **after** each server tick, so either all data gets saved or none.

Existing vanilla worlds can be converted to Cesium's storage format using the in-game world converter and back to vanilla's format.


## ♻️ Converting worlds
The world converter is accessible in the "Edit World" screen.

The converter does not delete any files, so your world folder will take up more disk space after conversion, but you can delete all files and folders that are no longer needed:
- From Anvil to Cesium: `region/`, `entities/`, `poi/`, `advancements/`, `stats/` and `playerdata/`.
- From Cesium to Anvil: `chunks.db` and `players.db`.

**Create a backup before modifying any files or running the converter!**

## 🧰 Building from sources
Cesium requires you to have at least JDK 21 installed. To build the project, run the following command in the project's root directory:
``./gradlew build``. If you don't want gradle to leave a daemon after building the project, append the ``--no-daemon`` flag.
The native libraries can be found [here](https://github.com/yamayaki1/cesium-natives) and can be build using the contained build script (Zig toolchain required).

## 🧑‍🤝‍🧑 Credits
This mod is based on [JellySquid‘s work](https://github.com/jellysquid3/radon-fabric) and uses the following native libraries:
- [facebook/zstd](https://github.com/facebook/zstd), BSD License. Copyright (c) Meta Platforms, Inc.
- [OpenLDAP/lmdb](https://github.com/LMDB/lmdb), The OpenLDAP Public License. Copyright (c) The OpenLDAP Foundation
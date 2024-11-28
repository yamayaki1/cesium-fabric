# Cesium for Fabric
Cesium is a mod designed to improve some flaws of Minecraft’s storage.
This mod implements transactional data storage and uses more efficient data compression.

---

> ### ⚠️ Disclaimer 
> 
> This mod is in an experimental state and could damage your world files. While there are no knows cases of world corruption happening because of this mod, bugs might happen.
> 
> Take regular backups of your worlds!

---

## ❓ What does this mod do?
With this mod, when chunks and/ or players are being saved, an efficient compression algorithm is used to compress data down to a reasonable size, before it's stored in-memory.

After each server tick, said data is written to disk in a transactional manner, meaning either all data is saved or none at all. This reduces scenarios where saved chunk data and player data become out-of-sync.

Advanced users can even disable compression at all, if they want to take advantage on transparent filesystem compression.


## ♻️ How do I convert worlds?
The world converter is accessible in the "Edit World" screen.

The converter does not delete any files, so your world folder will take up more disk space after conversion, but you can delete all files and folders that are no longer needed:
- From Anvil to Cesium: `region/`, `entities/`, `poi/`, `advancements/`, `stats/` and `playerdata/`.
- From Cesium to Anvil: `chunks.db` and `players.db`.

**Create a backup before modifying any files or running the converter!**


## 🧰 Building from sources
Cesium requires you to have at least JDK 21 installed. To build the project, run the following command in the project's root directory:
``./gradlew build``. If you don't want gradle to leave a daemon after building the project, append the ``--no-daemon`` flag.


## 🧑‍🤝‍🧑 Credits
This mod is based on [JellySquid‘s work](https://github.com/jellysquid3/radon-fabric) and uses the following native libraries:
- [facebook/zstd](https://github.com/facebook/zstd), BSD License. Copyright (c) Meta Platforms, Inc.
- [OpenLDAP/lmdb](https://github.com/LMDB/lmdb), The OpenLDAP Public License. Copyright (c) The OpenLDAP Foundation.
- #### Libraries provided by:
  - [lmdbjava/lmdbjava](https://github.com/lmdbjava/lmdbjava), Apache 2.0 License. Copyright (c) LmdbJava contributors.
  - [luben/zstd-jni](https://github.com/luben/zstd-jni), BSD License. Copyright (c) Luben Karavelov.
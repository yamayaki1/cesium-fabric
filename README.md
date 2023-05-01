# Cesium for Fabric
Cesium is a mod designed to improve the flaws of Minecraft’s Anvil Storage Format. It uses LMDB as its storage backend and utilises zstd compression for chunk data.

# Disclaimer
This mod is in an experimental state and could damage your world files. I *will try* to fix any bugs that arise, however, I *cannot guarantee* that I am able to do so.
**It is more a proof of concept than a stable product.**

## What it does
It creates a database for player data and a separate database for each dimension, which contains all chunk data (chunks itself, POI, entities).
- It can reduce used disk space by applying zstd compression to world files.
- You can optionally convert existing Anvil worlds to Cesium's format and vice-versa.
- It uses transactions to try prevent out-of-sync data on crashes.


### Converting worlds
The world converter is accessible in the "Edit World" screen (see screenshot below). You might have to resize your game window as the button placement is more than suboptimal currently.

<img src="https://cdn.modrinth.com/data/2fspKUWt/images/86f26e2932c86a0204732ac1d3108ccac8ae8492.png">

The converter does not delete any files, so your world folder will take up more disk space after conversion, but you can delete all files and folders that are no longer needed ("region", "entities", "poi", "advancements", "stats", "playerdata" when converting from Anvil to Cesium; "chunks.db" and "players.db" when converting a world from Cesium to Anvil). **Create a backup before modifying any files or running the converter!**

<img src="https://cdn.modrinth.com/data/2fspKUWt/images/7ae518edf6dd1cc3cd0837a6de437b26ffa78b26.png">

## What it isn’t
- It is not stable, please do not use it in a production environment or if you care about your world.
- It currently does not try to be faster than vanilla’s chunk storage.

## Building from sources
Cesium requires you to have at least JDK 17 installed. To build the project, run the following command in the project's root directory:
``./gradlew build``. If you don't want gradle to leave a daemon after building the project, append the ``--no-daemon`` flag.

## Credits
This mod is based on JellySquid‘s work (https://github.com/jellysquid3/radon-fabric) and uses the following 3rd-party bindings:
- https://github.com/luben/zstd-jni
- https://github.com/lmdbjava/lmdbjava
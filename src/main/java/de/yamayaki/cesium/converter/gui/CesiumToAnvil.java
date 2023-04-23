package de.yamayaki.cesium.converter.gui;

import de.yamayaki.cesium.converter.ConvHelper;
import de.yamayaki.cesium.converter.IChunkStorage;
import de.yamayaki.cesium.converter.IPlayerStorage;
import de.yamayaki.cesium.converter.formats.anvil.AnvilChunkStorage;
import de.yamayaki.cesium.converter.formats.anvil.AnvilPlayerStorage;
import de.yamayaki.cesium.converter.formats.cesium.CesiumChunkStorage;
import de.yamayaki.cesium.converter.formats.cesium.CesiumPlayerStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldStem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lmdbjava.LmdbException;

import java.nio.file.Path;

public class CesiumToAnvil {
    private final Logger logger = LogManager.getLogger("Cesium");

    public static void convertWorld(final Minecraft minecraft, final LevelStorageSource.LevelStorageAccess levelAccess) {
        final CesiumToAnvil converter = new CesiumToAnvil();

        // convert playerdata
        final Path playerDataPath = levelAccess.getDimensionPath(Level.OVERWORLD);
        converter.importPlayerData(playerDataPath);

        // convert chunkdata
        try (WorldStem worldStem = minecraft.createWorldOpenFlows().loadWorldStem(levelAccess, false)) {
            RegistryAccess.Frozen frozen = worldStem.registries().compositeAccess();
            levelAccess.saveDataTag(frozen, worldStem.worldData());

            Registry<LevelStem> registry = frozen.registryOrThrow(Registries.LEVEL_STEM);
            registry.registryKeySet().stream().map(Registries::levelStemToLevel).forEach(levelKey -> {
                converter.importLevel(levelAccess, levelKey);
            });
        } catch (Exception exception) {
            if (exception instanceof LmdbException lmdbException) {
                converter.logger.error("Something strange happenend while accessing the database", lmdbException);
                return;
            }

            throw new RuntimeException(exception);
        }
    }

    private void importPlayerData(final Path basePath) {
        final IPlayerStorage originalStorage = new CesiumPlayerStorage(this.logger, basePath);
        final IPlayerStorage newStorage = new AnvilPlayerStorage(this.logger, basePath);

        ConvHelper.transferPlayerData(this.logger, originalStorage, newStorage);

        originalStorage.close();
        newStorage.close();
    }

    private void importLevel(final LevelStorageSource.LevelStorageAccess levelAccess, final ResourceKey<Level> level) {
        final Path dimensionPath = levelAccess.getDimensionPath(level);

        final IChunkStorage originalStorage = new CesiumChunkStorage(this.logger, dimensionPath);
        final IChunkStorage newStorage = new AnvilChunkStorage(this.logger, dimensionPath);

        logger.info("Transfering chunks for dimension {}", dimensionPath.toString());
        ConvHelper.transferChunkData(this.logger, originalStorage, newStorage);

        originalStorage.close();
        newStorage.close();
    }
}

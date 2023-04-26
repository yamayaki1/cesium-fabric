package de.yamayaki.cesium.converter.gui;

import de.yamayaki.cesium.CesiumMod;
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
import org.lmdbjava.LmdbException;

import java.nio.file.Path;

public class AnvilToCesium {
    @SuppressWarnings("CodeBlock2Expr")
    public static void convertWorld(final Minecraft minecraft, final LevelStorageSource.LevelStorageAccess levelAccess) {
        final AnvilToCesium converter = new AnvilToCesium();

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
                CesiumMod.logger().error("Something strange happenend while accessing the database", lmdbException);
                return;
            }

            throw new RuntimeException(exception);
        }
    }

    private void importPlayerData(final Path basePath) {
        final IPlayerStorage originalStorage = new AnvilPlayerStorage(basePath);
        final IPlayerStorage newStorage = new CesiumPlayerStorage(basePath);

        ConvHelper.transferPlayerData(originalStorage, newStorage);

        originalStorage.close();
        newStorage.close();
    }

    private void importLevel(final LevelStorageSource.LevelStorageAccess levelAccess, final ResourceKey<Level> level) {
        final Path dimensionPath = levelAccess.getDimensionPath(level);

        final IChunkStorage originalStorage = new AnvilChunkStorage(dimensionPath);
        final IChunkStorage newStorage = new CesiumChunkStorage(dimensionPath);

        CesiumMod.logger().info("Transfering chunks for dimension {}", dimensionPath.toString());
        ConvHelper.transferChunkData(originalStorage, newStorage);

        originalStorage.close();
        newStorage.close();
    }
}

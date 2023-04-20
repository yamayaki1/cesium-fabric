package me.jellysquid.mods.radon.converter.gui;

import me.jellysquid.mods.radon.common.db.lightning.LmdbException;
import me.jellysquid.mods.radon.converter.ConvHelper;
import me.jellysquid.mods.radon.converter.IChunkStorage;
import me.jellysquid.mods.radon.converter.IPlayerStorage;
import me.jellysquid.mods.radon.converter.formats.anvil.AnvilChunkStorage;
import me.jellysquid.mods.radon.converter.formats.anvil.AnvilPlayerStorage;
import me.jellysquid.mods.radon.converter.formats.radon.RadonChunkStorage;
import me.jellysquid.mods.radon.converter.formats.radon.RadonPlayerStorage;
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

import java.nio.file.Path;

public class AnvilToRadon {
    private final Logger logger = LogManager.getLogger("Radon");

    public static void convertWorld(Minecraft minecraft, LevelStorageSource.LevelStorageAccess levelAccess) {
        AnvilToRadon converter = new AnvilToRadon();

        // convert playerdata
        Path playerDataPath = levelAccess.getDimensionPath(Level.OVERWORLD);
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

    private void importPlayerData(Path basePath) {
        IPlayerStorage originalStorage = new AnvilPlayerStorage(this.logger, basePath);
        IPlayerStorage newStorage = new RadonPlayerStorage(this.logger, basePath);

        ConvHelper.transferPlayerData(this.logger, originalStorage, newStorage);

        originalStorage.close();
        newStorage.close();
    }

    private void importLevel(LevelStorageSource.LevelStorageAccess levelAccess, ResourceKey<Level> level) {
        Path dimensionPath = levelAccess.getDimensionPath(level);

        IChunkStorage originalStorage = new AnvilChunkStorage(this.logger, dimensionPath);
        IChunkStorage newStorage = new RadonChunkStorage(this.logger, dimensionPath);

        logger.info("Transfering chunks for dimension {}", dimensionPath.toString());
        ConvHelper.transferChunkData(this.logger, originalStorage, newStorage);

        originalStorage.close();
        newStorage.close();
    }
}

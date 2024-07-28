package de.yamayaki.cesium.maintenance.storage.anvil;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.maintenance.FileHelper;
import de.yamayaki.cesium.maintenance.storage.IPlayerStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class AnvilPlayerStorage implements IPlayerStorage {
    private final Path playerData;
    private final Path statsStorage;
    private final Path advancementsStorage;

    public AnvilPlayerStorage(final Path basePath) {
        this.playerData = basePath.resolve("playerdata");
        this.statsStorage = basePath.resolve("stats");
        this.advancementsStorage = basePath.resolve("advancements");
    }

    @Override
    public List<UUID> getAllPlayers() {
        return FileHelper.resolveAllEnding(this.playerData, ".dat").stream().map(file -> {
            try {
                final String fileName = file.getName();
                final String uuid = fileName.substring(0, fileName.length() - 4);

                return UUID.fromString(uuid);
            } catch (final Throwable t) {
                CesiumMod.logger().error("Could not resolve UUID from filename, ignoring ({})", file.getAbsolutePath());
                throw t;
            }
        }).toList();
    }

    @Override
    public void close() {

    }

    @Override
    public void setPlayerNBT(final UUID uuid, final CompoundTag compoundTag) {
        if (compoundTag == null) {
            return;
        }

        try {
            final Path savePath = this.playerData.resolve(uuid.toString() + ".dat");
            if (!Files.isDirectory(savePath.getParent()) || !Files.isRegularFile(savePath)) {
                Files.createDirectories(savePath.getParent());
                Files.createFile(savePath);
            }

            NbtIo.writeCompressed(compoundTag, savePath);
        } catch (IOException exception) {
            CesiumMod.logger().warn("[ANVIL] Failed to save player data for {}", uuid);
        }
    }

    @Override
    public CompoundTag getPlayerNBT(final UUID uuid) {
        CompoundTag compoundTag = null;

        try {
            final Path savePath = this.playerData.resolve(uuid.toString() + ".dat");
            if (Files.isRegularFile(savePath)) {
                compoundTag = NbtIo.readCompressed(savePath, NbtAccounter.unlimitedHeap());
            }
        } catch (IOException exception) {
            CesiumMod.logger().warn("[ANVIL] Failed to load player data for {}", uuid);
        }

        return compoundTag;
    }

    @Override
    public void setPlayerAdvancements(final UUID uuid, final String advancements) {
        try {
            final Path savePath = this.advancementsStorage.resolve(uuid.toString() + ".json");
            FileHelper.saveToFile(savePath, advancements);
        } catch (IOException exception) {
            CesiumMod.logger().warn("[ANVIL] Failed to set advancements for {}", uuid);
        }
    }

    @Override
    public String getPlayerAdvancements(final UUID uuid) {
        String advancements = null;
        try {
            final Path savePath = this.advancementsStorage.resolve(uuid.toString() + ".json");
            advancements = FileHelper.getContents(savePath);
        } catch (IOException exception) {
            CesiumMod.logger().warn("[ANVIL] Failed to load advancements for {}", uuid);
        }

        return advancements;
    }

    @Override
    public void setPlayerStatistics(final UUID uuid, final String statistics) {
        try {
            final Path savePath = this.statsStorage.resolve(uuid.toString() + ".json");
            FileHelper.saveToFile(savePath, statistics);
        } catch (IOException exception) {
            CesiumMod.logger().warn("[ANVIL] Failed to save statistics for {}", uuid);
        }
    }

    @Override
    public String getPlayerStatistics(final UUID uuid) {
        String statistics = null;
        try {
            final Path savePath = this.statsStorage.resolve(uuid.toString() + ".json");
            statistics = FileHelper.getContents(savePath);
        } catch (IOException exception) {
            CesiumMod.logger().warn("[ANVIL] Failed to load statistics for {}", uuid);
        }

        return statistics;
    }
}

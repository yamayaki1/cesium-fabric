package me.jellysquid.mods.radon.converter.formats.anvil;

import me.jellysquid.mods.radon.converter.ConvHelper;
import me.jellysquid.mods.radon.converter.IPlayerStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class AnvilPlayerStorage implements IPlayerStorage {
    private final Logger logger;

    private final Path playerData;
    private final Path statsStorage;
    private final Path advancementsStorage;

    public AnvilPlayerStorage(Logger logger, Path basePath) {
        this.logger = logger;

        this.playerData = basePath.resolve("playerdata");
        this.statsStorage = basePath.resolve("stats");
        this.advancementsStorage = basePath.resolve("advancements");
    }

    @Override
    public List<UUID> getAllPlayers() {
        return ConvHelper.resolveAllEnding(this.playerData, ".dat").stream().map(file -> {
            String fileName = file.getName();
            String uuid = fileName.substring(0, fileName.length() - 4);

            return UUID.fromString(uuid);
        }).toList();
    }

    @Override
    public void close() {

    }

    @Override
    public void setPlayerNBT(UUID uuid, CompoundTag compoundTag) {
        try {
            File saveFile = new File(this.playerData.toFile(), uuid.toString() + ".dat");
            NbtIo.writeCompressed(compoundTag, saveFile);
        } catch (IOException exception) {
            this.logger.warn("[ANVIL] Failed to save player data for {}", uuid);
        }
    }

    @Override
    public CompoundTag getPlayerNBT(UUID uuid) {
        CompoundTag compoundTag = null;

        try {
            File saveFile = new File(this.playerData.toFile(), uuid.toString() + ".dat");
            if (saveFile.exists() && saveFile.isFile()) {
                compoundTag = NbtIo.readCompressed(saveFile);
            }
        } catch (IOException exception) {
            this.logger.warn("[ANVIL] Failed to load player data for {}", uuid);
        }

        return compoundTag;
    }

    @Override
    public void setPlayerAdvancements(UUID uuid, String advancements) {
        try {
            Path savePath = this.advancementsStorage.resolve(uuid.toString() + ".json");
            ConvHelper.saveToFile(savePath, advancements);
        } catch (IOException exception) {
            this.logger.warn("[ANVIL] Failed to load advancements for {}", uuid);
        }
    }

    @Override
    public String getPlayerAdvancements(UUID uuid) {
        String advancements = null;
        try {
            Path savePath = this.advancementsStorage.resolve(uuid.toString() + ".json");
            advancements = ConvHelper.getContents(savePath);
        } catch (IOException exception) {
            this.logger.warn("[ANVIL] Failed to load advancements for {}", uuid);
        }

        return advancements;
    }

    @Override
    public void setPlayerStatistics(UUID uuid, String statistics) {
        try {
            Path savePath = this.statsStorage.resolve(uuid.toString() + ".json");
            ConvHelper.saveToFile(savePath, statistics);
        } catch (IOException exception) {
            this.logger.warn("[ANVIL] Failed to load advancements for {}", uuid);
        }
    }

    @Override
    public String getPlayerStatistics(UUID uuid) {
        String statistics = null;
        try {
            Path savePath = this.statsStorage.resolve(uuid.toString() + ".json");
            statistics = ConvHelper.getContents(savePath);
        } catch (IOException exception) {
            this.logger.warn("[ANVIL] Failed to load advancements for {}", uuid);
        }

        return statistics;
    }
}

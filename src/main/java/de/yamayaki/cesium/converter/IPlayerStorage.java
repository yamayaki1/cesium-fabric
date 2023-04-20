package de.yamayaki.cesium.converter;

import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.UUID;

public interface IPlayerStorage {
    List<UUID> getAllPlayers();

    void close();

    /**
     * PlayerData
     **/
    void setPlayerNBT(UUID uuid, CompoundTag compoundTag);

    CompoundTag getPlayerNBT(UUID uuid);

    /**
     * PlayerAdvancements
     **/
    void setPlayerAdvancements(UUID uuid, String advancements);

    String getPlayerAdvancements(UUID uuid);

    /**
     * PlayerStatistics
     **/
    void setPlayerStatistics(UUID uuid, String statistics);

    String getPlayerStatistics(UUID uuid);
}

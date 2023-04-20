package me.jellysquid.mods.radon.converter.formats.radon;

import me.jellysquid.mods.radon.common.db.LMDBInstance;
import me.jellysquid.mods.radon.common.db.spec.DatabaseSpec;
import me.jellysquid.mods.radon.common.db.spec.impl.PlayerDatabaseSpecs;
import me.jellysquid.mods.radon.converter.IPlayerStorage;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class RadonPlayerStorage implements IPlayerStorage {
    private final Logger logger;

    private final LMDBInstance database;

    public RadonPlayerStorage(Logger logger, Path basePath) {
        this.logger = logger;

        this.database = new LMDBInstance(basePath.toFile(), "players", new DatabaseSpec[]{
                PlayerDatabaseSpecs.PLAYER_DATA,
                PlayerDatabaseSpecs.ADVANCEMENTS,
                PlayerDatabaseSpecs.STATISTICS
        });
    }

    @Override
    public List<UUID> getAllPlayers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        this.database.flushChanges();
        this.database.close();
    }

    @Override
    public void setPlayerNBT(UUID uuid, CompoundTag compoundTag) {
        this.database.getTransaction(PlayerDatabaseSpecs.PLAYER_DATA).add(uuid, compoundTag);
    }

    @Override
    public CompoundTag getPlayerNBT(UUID uuid) {
        return this.database.getDatabase(PlayerDatabaseSpecs.PLAYER_DATA).getValue(uuid);
    }

    @Override
    public void setPlayerAdvancements(UUID uuid, String advancements) {
        this.database.getTransaction(PlayerDatabaseSpecs.ADVANCEMENTS).add(uuid, advancements);
    }

    @Override
    public String getPlayerAdvancements(UUID uuid) {
        return this.database.getDatabase(PlayerDatabaseSpecs.ADVANCEMENTS).getValue(uuid);
    }

    @Override
    public void setPlayerStatistics(UUID uuid, String statistics) {
        this.database.getTransaction(PlayerDatabaseSpecs.STATISTICS).add(uuid, statistics);
    }

    @Override
    public String getPlayerStatistics(UUID uuid) {
        return this.database.getDatabase(PlayerDatabaseSpecs.STATISTICS).getValue(uuid);
    }
}

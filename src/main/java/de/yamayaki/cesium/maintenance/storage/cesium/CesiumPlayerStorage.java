package de.yamayaki.cesium.maintenance.storage.cesium;

import de.yamayaki.cesium.common.db.LMDBInstance;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import de.yamayaki.cesium.common.db.spec.impl.PlayerDatabaseSpecs;
import de.yamayaki.cesium.maintenance.storage.IPlayerStorage;
import net.minecraft.nbt.CompoundTag;
import org.lmdbjava.Cursor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CesiumPlayerStorage implements IPlayerStorage {
    private final LMDBInstance database;

    public CesiumPlayerStorage(final Path basePath) {
        this.database = new LMDBInstance(basePath, "players", new DatabaseSpec[]{
                PlayerDatabaseSpecs.PLAYER_DATA,
                PlayerDatabaseSpecs.ADVANCEMENTS,
                PlayerDatabaseSpecs.STATISTICS
        });
    }

    @Override
    public List<UUID> getAllPlayers() {
        final List<UUID> list = new ArrayList<>();

        final Cursor<byte[]> cursor = this.database.getDatabase(PlayerDatabaseSpecs.STATISTICS)
                .getIterator();

        boolean exists = cursor.first();
        while (exists) {
            final UUID uid = this.database.getDatabase(PlayerDatabaseSpecs.STATISTICS)
                    .getKeySerializer()
                    .deserializeKey(cursor.key());

            list.add(uid);
            exists = cursor.next();
        }

        cursor.close();
        return list;
    }

    @Override
    public void close() {
        this.database.flushChanges();
        this.database.close();
    }

    @Override
    public void setPlayerNBT(final UUID uuid, final CompoundTag compoundTag) {
        this.database.getTransaction(PlayerDatabaseSpecs.PLAYER_DATA).add(uuid, compoundTag);
    }

    @Override
    public CompoundTag getPlayerNBT(final UUID uuid) {
        return this.database.getDatabase(PlayerDatabaseSpecs.PLAYER_DATA).getValue(uuid);
    }

    @Override
    public void setPlayerAdvancements(final UUID uuid, final String advancements) {
        this.database.getTransaction(PlayerDatabaseSpecs.ADVANCEMENTS).add(uuid, advancements);
    }

    @Override
    public String getPlayerAdvancements(final UUID uuid) {
        return this.database.getDatabase(PlayerDatabaseSpecs.ADVANCEMENTS).getValue(uuid);
    }

    @Override
    public void setPlayerStatistics(final UUID uuid, final String statistics) {
        this.database.getTransaction(PlayerDatabaseSpecs.STATISTICS).add(uuid, statistics);
    }

    @Override
    public String getPlayerStatistics(final UUID uuid) {
        return this.database.getDatabase(PlayerDatabaseSpecs.STATISTICS).getValue(uuid);
    }
}

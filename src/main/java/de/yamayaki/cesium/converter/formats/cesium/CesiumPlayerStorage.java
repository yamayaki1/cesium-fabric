package de.yamayaki.cesium.converter.formats.cesium;

import de.yamayaki.cesium.common.db.lightning.Csr;
import de.yamayaki.cesium.common.db.spec.DatabaseSpec;
import de.yamayaki.cesium.common.db.spec.impl.PlayerDatabaseSpecs;
import de.yamayaki.cesium.converter.IPlayerStorage;
import de.yamayaki.cesium.common.db.LMDBInstance;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CesiumPlayerStorage implements IPlayerStorage {
    private final Logger logger;

    private final LMDBInstance database;

    public CesiumPlayerStorage(final Logger logger, final Path basePath) {
        this.logger = logger;

        this.database = new LMDBInstance(basePath.toFile(), "players", new DatabaseSpec[]{
                PlayerDatabaseSpecs.PLAYER_DATA,
                PlayerDatabaseSpecs.ADVANCEMENTS,
                PlayerDatabaseSpecs.STATISTICS
        });
    }

    @Override
    public List<UUID> getAllPlayers() {
        final List<UUID> list = new ArrayList<>();

        final Csr cursor = this.database.getDatabase(PlayerDatabaseSpecs.PLAYER_DATA)
                .getIterator();

        while (cursor.hasNext()) {
            final UUID uid = this.database.getDatabase(PlayerDatabaseSpecs.PLAYER_DATA)
                    .getKeySerializer().deserializeKey(cursor.next());
            list.add(uid);
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

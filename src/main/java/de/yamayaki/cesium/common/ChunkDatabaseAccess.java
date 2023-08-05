package de.yamayaki.cesium.common;

import de.yamayaki.cesium.common.db.LMDBInstance;

public interface ChunkDatabaseAccess {
    void cesium$setDatabase(LMDBInstance database);
}

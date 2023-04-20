package de.yamayaki.cesium.common;

import de.yamayaki.cesium.common.db.LMDBInstance;

public interface ChunkDatabaseAccess {
    void setDatabase(LMDBInstance database);
}

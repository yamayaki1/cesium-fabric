package de.yamayaki.cesium.accessor;

import de.yamayaki.cesium.common.db.LMDBInstance;

public interface DatabaseSetter {
    void cesium$setStorage(LMDBInstance lmdbInstance);
}

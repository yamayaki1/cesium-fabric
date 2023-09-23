package de.yamayaki.cesium.accessor;

import de.yamayaki.cesium.common.db.LMDBInstance;

public interface DatabaseSource {
    LMDBInstance cesium$getStorage();
}

package de.yamayaki.cesium.accessor;

import de.yamayaki.cesium.api.db.IDBInstance;

public interface DatabaseSource {
    IDBInstance cesium$getStorage();
}

package de.yamayaki.cesium.api.accessor;

import de.yamayaki.cesium.api.database.IDBInstance;

public interface DatabaseSource {
    IDBInstance cesium$getStorage();
}

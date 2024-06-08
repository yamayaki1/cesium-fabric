package de.yamayaki.cesium.api.accessor;

import de.yamayaki.cesium.api.database.IDBInstance;

public interface DatabaseSetter {
    void cesium$setStorage(final IDBInstance dbInstance);
}

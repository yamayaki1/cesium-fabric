package de.yamayaki.cesium.common.db;

public interface DatabaseItem {
    LMDBInstance cesium$getStorage();

    void cesium$setStorage(LMDBInstance holder);
}

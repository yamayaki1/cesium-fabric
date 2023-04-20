package de.yamayaki.cesium.common.db;

public interface DatabaseItem {
    LMDBInstance getStorage();

    void setStorage(LMDBInstance holder);
}

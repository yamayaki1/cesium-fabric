package me.jellysquid.mods.radon.common.db;

public interface DatabaseItem {
    LMDBInstance getStorage();

    void setStorage(LMDBInstance holder);
}

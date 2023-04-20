package de.yamayaki.cesium.common.db.lightning;

import org.lwjgl.system.MemoryStack;

@FunctionalInterface
interface Transaction<T> {
    T exec(MemoryStack stack, long txn);

}
package de.yamayaki.cesium.api.accessor;

import de.yamayaki.cesium.storage.IComponentStorage;
import org.jetbrains.annotations.NotNull;

public interface IStorageSetter<K, V> {
    void cesium$setStorage(final @NotNull IComponentStorage<K, V> componentStorage);
}

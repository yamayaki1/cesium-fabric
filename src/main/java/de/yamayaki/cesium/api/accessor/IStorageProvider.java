package de.yamayaki.cesium.api.accessor;

import de.yamayaki.cesium.storage.IComponentStorage;
import org.jetbrains.annotations.NotNull;

public interface IStorageProvider<K, V> {
    @NotNull IComponentStorage<K, V> cesium$storage();
}

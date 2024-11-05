package de.yamayaki.cesium.api.accessor;

import de.yamayaki.cesium.storage.IWorldStorage;
import org.jetbrains.annotations.NotNull;

public interface IWorldStorageGetter {
    @NotNull IWorldStorage cesium$worldStorage();
}

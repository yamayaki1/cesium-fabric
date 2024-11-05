package de.yamayaki.cesium.api.accessor;

import de.yamayaki.cesium.storage.IDimensionStorage;
import org.jetbrains.annotations.NotNull;

public interface IDimensionStorageGetter {
    @NotNull IDimensionStorage cesium$dimensionStorage();
}

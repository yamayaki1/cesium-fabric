package de.yamayaki.cesium.api.accessor;

import de.yamayaki.cesium.api.database.DatabaseSpec;

public interface SpecificationSetter {
    void cesium$setSpec(final DatabaseSpec<?, ?> databaseSpec);
}

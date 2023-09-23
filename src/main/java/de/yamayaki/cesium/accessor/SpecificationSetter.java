package de.yamayaki.cesium.accessor;

import de.yamayaki.cesium.common.db.spec.DatabaseSpec;

public interface SpecificationSetter {
    void cesium$setSpec(DatabaseSpec<?, ?> databaseSpec);
}

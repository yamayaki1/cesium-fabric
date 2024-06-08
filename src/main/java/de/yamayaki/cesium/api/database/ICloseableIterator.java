package de.yamayaki.cesium.api.database;

import java.util.Iterator;

public interface ICloseableIterator<K> extends Iterator<K>, AutoCloseable {
}

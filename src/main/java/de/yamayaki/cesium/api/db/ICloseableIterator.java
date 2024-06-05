package de.yamayaki.cesium.api.db;

import java.util.Iterator;

public interface ICloseableIterator<K> extends Iterator<K>, AutoCloseable {
}

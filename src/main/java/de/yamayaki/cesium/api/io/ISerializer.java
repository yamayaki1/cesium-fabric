package de.yamayaki.cesium.api.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface ISerializer<T> {
    byte @NotNull [] serialize(final @NotNull T input) throws IOException;

    default byte @NotNull [] serializeKey(final @NotNull T input) {
        try {
            return this.serialize(input);
        } catch (final IOException i) {
            throw new RuntimeException("Failed to serialize data!", i);
        }
    }

    @NotNull T deserialize(final byte @NotNull [] input) throws IOException;

    default @NotNull T deserializeKey(final byte @NotNull [] input) {
        try {
            return this.deserialize(input);
        } catch (final IOException i) {
            throw new RuntimeException("Failed to deserialize data!", i);
        }
    }
}

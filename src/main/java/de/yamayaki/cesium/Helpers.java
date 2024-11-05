package de.yamayaki.cesium;

import java.io.IOException;

public class Helpers {
    public static void throwingV(final ThrowingRunnable supplier) {
        try {
            supplier.run();
        } catch (final IOException i) {
            throw new RuntimeException("Could not supply value", i);
        }
    }

    public static <T> T throwing(final ThrowingSupplier<T> supplier) {
        try {
            return supplier.run();
        } catch (final IOException i) {
            throw new RuntimeException("Could not supply value", i);
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T run() throws IOException;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws IOException;
    }
}

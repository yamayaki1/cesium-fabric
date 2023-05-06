package de.yamayaki.cesium;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CesiumMod implements ModInitializer {
    private static Logger LOGGER;

    private static ExecutorService threadPool;

    @Override
    public void onInitialize() {
        LOGGER = LogManager.getLogger("Cesium");
        resetPool();
    }

    public static Logger logger() {
        return LOGGER;
    }

    public static void resetPool() {
        if (threadPool != null) {
            threadPool.shutdown();
            threadPool = null;
        }

        final int threadCount = Math.max(Runtime.getRuntime().availableProcessors() / 2, 4);
        threadPool = Executors.newFixedThreadPool(threadCount, new ThreadFactory() {
            final AtomicInteger atomicInteger = new AtomicInteger(1);

            @Override
            public Thread newThread(@NotNull Runnable runnable) {
                return new Thread(runnable, "Cesium#" + atomicInteger.getAndIncrement());
            }
        });
    }

    public static ExecutorService getPool() {
        if (threadPool == null || threadPool.isShutdown() || threadPool.isTerminated()) {
            throw new RuntimeException();
        }

        return threadPool;
    }
}

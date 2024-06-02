package de.yamayaki.cesium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class CesiumMod implements ModInitializer {
    private static Logger LOGGER;
    private static CesiumConfig CONFIG;

    public static Logger logger() {
        return LOGGER;
    }

    @Override
    public void onInitialize() {
        LOGGER = LogManager.getLogger("Cesium");
        initConfig();
    }

    public static CesiumConfig config() {
        if (CONFIG == null) {
            throw new RuntimeException("Config is not yet available, did you do something silly?");
        }

        return CONFIG;
    }

    public static String getFileEnding() {
        return CONFIG.isUncompressed() ? ".uncompressed.db" : ".db";
    }

    private static void initConfig() {
        final Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .setLenient()
                .create();

        final Path path = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("cesium.json");

        try {
            loadConfig(gson, path); // Load from disk or create new default instance
            saveConfig(gson, path); // Save to disk and/ or add missing fields
        } catch (final IOException ioException) {
            throw new RuntimeException("Failed to initialize Cesium config.", ioException);
        }
    }

    private static void loadConfig(final Gson gson, final Path path) throws IOException {
        if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            try (final BufferedReader bufferedReader = Files.newBufferedReader(path)) {
                CONFIG = gson.fromJson(bufferedReader, CesiumConfig.class);
            }
        } else {
            CONFIG = new CesiumConfig();
        }
    }

    private static void saveConfig(final Gson gson, final Path path) throws IOException {
        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            gson.toJson(CONFIG, CesiumConfig.class, gson.newJsonWriter(bufferedWriter));
        }
    }
}

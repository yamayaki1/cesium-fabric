package de.yamayaki.cesium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CesiumConfig {
    public int dataVersion = 2;
    public GeneralOptions general = new GeneralOptions();
    public CompressionOptions compression = new CompressionOptions();
    public ExperimentalOptions experimental = new ExperimentalOptions();

    public static class GeneralOptions {
        public boolean showDebugInformation = false;
        public boolean logMapGrows = false;
    }

    public static class CompressionOptions {
        public boolean enableCompression = true;
        public ZSTDOptions zstd = new ZSTDOptions();

        public static class ZSTDOptions {
            public boolean enableDictionary = true;
            public int compressionLevel = 8;
        }
    }

    public static class ExperimentalOptions {
        public boolean forceSaveDataAfterTick = false;
    }

    public static class Loader {
        private static final Gson GSON = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .setLenient()
                .create();

        public static CesiumConfig load(final @NotNull Path configPath, final @NotNull Logger logger) {
            CesiumConfig config = read(configPath, logger);

            if (config == null) {
                config = new CesiumConfig();
            }

            write(config, configPath);

            return config;
        }

        private static CesiumConfig read(final @NotNull Path configPath, final @NotNull Logger logger) {
            if (Files.isRegularFile(configPath, LinkOption.NOFOLLOW_LINKS)) {
                try (final BufferedReader bufferedReader = Files.newBufferedReader(configPath)) {
                    return GSON.fromJson(bufferedReader, CesiumConfig.class);
                } catch (final IOException | JsonSyntaxException i) {
                    logger.error("Could not read config file! Resetting ...", i);
                }
            }

            return null;
        }

        private static void write(final @NotNull CesiumConfig config, final @NotNull Path configPath) {
            try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(configPath, LinkOption.NOFOLLOW_LINKS, StandardOpenOption.CREATE)) {
                GSON.toJson(config, CesiumConfig.class, bufferedWriter);
            } catch (final IOException i) {
                throw new RuntimeException("Could not write config file!", i);
            }
        }
    }
}

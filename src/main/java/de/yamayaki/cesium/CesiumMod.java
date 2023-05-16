package de.yamayaki.cesium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.yamayaki.cesium.common.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CesiumMod implements ModInitializer {
    private static Logger LOGGER;
    private static Config CONFIG;

    public static Logger logger() {
        return LOGGER;
    }

    @Override
    public void onInitialize() {
        LOGGER = LogManager.getLogger("Cesium");
        loadConfig();
    }

    public static Config config() {
        if (CONFIG == null) {
            throw new RuntimeException("Config is not yet available, did you do something silly?");
        }

        return CONFIG;
    }

    private static void loadConfig() {
        final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().setLenient().create();
        final File config = new File(FabricLoader.getInstance().getConfigDir().toFile(), "cesium.json");

        if (config.exists() && config.isFile()) {
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(config))) {
                final String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                CONFIG = gson.fromJson(json, Config.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            CONFIG = new Config();
        }

        try (FileWriter fileWriter = new FileWriter(config)) {
            final String json = gson.toJson(CONFIG, Config.class);
            fileWriter.write(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

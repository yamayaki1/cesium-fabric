package de.yamayaki.cesium;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CesiumMod implements ModInitializer {
    private static Logger LOGGER;

    public static Logger logger() {
        return LOGGER;
    }

    @Override
    public void onInitialize() {
        LOGGER = LogManager.getLogger("Cesium");
    }
}

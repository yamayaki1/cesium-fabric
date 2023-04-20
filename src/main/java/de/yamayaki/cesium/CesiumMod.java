package de.yamayaki.cesium;

import de.yamayaki.cesium.common.dep.DependencyExtractor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class CesiumMod implements ModInitializer {
    @Override
    public void onInitialize() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            loadNatives();
        }
    }

    private void loadNatives() {
        DependencyExtractor.installLwjglNatives("lwjgl-lmdb", "3.3.1");
        DependencyExtractor.installLwjglNatives("lwjgl-zstd", "3.3.1");
    }
}

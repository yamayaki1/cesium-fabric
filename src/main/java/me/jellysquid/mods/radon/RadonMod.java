package me.jellysquid.mods.radon;

import me.jellysquid.mods.radon.common.dep.DependencyExtractor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class RadonMod implements ModInitializer {
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

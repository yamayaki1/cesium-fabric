package de.yamayaki.cesium.mixin.base;

import com.mojang.datafixers.DataFixer;
import de.yamayaki.cesium.api.accessor.IWorldStorageGetter;
import de.yamayaki.cesium.storage.IWorldStorage;
import de.yamayaki.cesium.storage.impl.CesiumWorldStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements IWorldStorageGetter {
    /* Vanilla imports */
    @Shadow @Final private static Logger LOGGER;
    @Shadow public abstract Path getWorldPath(LevelResource levelResource);

    /* Our world storage */
    @Unique private IWorldStorage worldStorage = null;

    /* Fields related to the async save feature */
    @Unique private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Cesium-Async-Save"));
    @Unique private CompletableFuture<Void> saveFuture = null;
    @Unique private boolean didError = false;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    protected void cesium$createWorldStorage(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
        if (this.worldStorage != null) {
            throw new IllegalStateException("Something went HORRIBLY wrong! WorldStorage already exists!");
        }

        this.worldStorage = new CesiumWorldStorage(this.getWorldPath(LevelResource.ROOT));
    }

    @Inject(
            method = "tickServer",
            at = @At("RETURN")
    )
    protected void cesium$afterServerTick(final CallbackInfo ci) {
        if (this.worldStorage == null) {
            return;
        }

        // Kill immediately when an error occurred,
        // we can't assume a healthy environment anymore.
        if (this.didError) {
            System.exit(-202411);
        }

        if (this.saveFuture != null && !this.saveFuture.isDone()) {
            return;
        }

        this.saveFuture = CompletableFuture.runAsync(
                () -> this.worldStorage.flush(), this.saveExecutor
        ).exceptionally(throwable -> {
            LOGGER.error("Failed to sync all data!", throwable);
            this.didError = true;

            return null;
        });
    }

    @Inject(
            method = "stopServer",
            at = @At("RETURN")
    )
    protected void cesium$destroyWorldStorage(final CallbackInfo ci) {
        if (this.worldStorage == null) {
            return;
        }

        if (this.saveFuture != null && !this.didError) {
            this.saveFuture.join();
        }

        this.saveExecutor.shutdownNow();

        this.worldStorage.close();
        this.worldStorage = null;
    }

    @Override
    public @NotNull IWorldStorage cesium$worldStorage() {
        if (this.worldStorage == null) {
            throw new IllegalStateException("WorldStorage has not been initialized yet!");
        }

        return this.worldStorage;
    }
}

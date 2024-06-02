package de.yamayaki.cesium.mixin.convert_server;

import com.llamalad7.mixinextras.sugar.Local;
import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.converter.WorldConverter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.Main;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinMain {
    @Unique
    private static OptionSpec<Void> cesium$convertOptionAnvil;
    @Unique
    private static OptionSpec<Void> cesium$convertOptionCesium;

    @Inject(method = "main", at = @At(value = "INVOKE_ASSIGN", target = "Ljoptsimple/OptionParser;accepts(Ljava/lang/String;)Ljoptsimple/OptionSpecBuilder;", ordinal = 0), remap = false)
    private static void addConvertOption(String[] strings, CallbackInfo ci, @Local(ordinal = 0) OptionParser optionParser) {
        cesium$convertOptionAnvil = optionParser.accepts("cesiumConvertToAnvil");
        cesium$convertOptionCesium = optionParser.accepts("cesiumConvertToCesium");
    }

    @Inject(method = "main", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/core/LayeredRegistryAccess;compositeAccess()Lnet/minecraft/core/RegistryAccess$Frozen;", shift = At.Shift.AFTER))
    private static void doConvert(String[] strings, CallbackInfo ci, @Local OptionSet optionSet, @Local LevelStorageSource.LevelStorageAccess levelAccess, @Local RegistryAccess.Frozen registryAccess) {
        final boolean convertAnvil;

        if ((convertAnvil = optionSet.has(cesium$convertOptionAnvil)) || optionSet.has(cesium$convertOptionCesium)) {
            final WorldConverter.Format desiredFormat = convertAnvil ? WorldConverter.Format.TO_ANVIL : WorldConverter.Format.TO_CESIUM;
            doWorldConversion(desiredFormat, levelAccess, registryAccess);
        }
    }

    @Unique
    private static void doWorldConversion(final WorldConverter.Format desiredFormat, final LevelStorageSource.LevelStorageAccess levelAccess, final RegistryAccess registryAccess) {
        final Logger logger = CesiumMod.logger();
        logger.info("Starting world conversion ...");

        final WorldConverter worldConverter = new WorldConverter(desiredFormat, levelAccess, registryAccess);

        String previousStatus = null;
        String currentStatus;

        while (worldConverter.running()) {
            currentStatus = worldConverter.status();

            if (currentStatus != null && !currentStatus.equals(previousStatus)) {
                previousStatus = currentStatus;
                logger.info(currentStatus);
            }

            logger.info("{}% completed ({} / {} elements) ...", Math.floor(worldConverter.percentage() * 100), worldConverter.currentElement(), worldConverter.totalElements());

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ignored) {
            }
        }
    }
}

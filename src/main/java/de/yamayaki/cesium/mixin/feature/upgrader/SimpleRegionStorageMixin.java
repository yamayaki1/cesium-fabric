package de.yamayaki.cesium.mixin.feature.upgrader;

import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SimpleRegionStorage.class)
public class SimpleRegionStorageMixin {
    @Redirect(
            method = "upgradeChunkTag(Lnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/datafix/DataFixTypes;updateToCurrentVersion(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    public CompoundTag cesium$fix_addCurrentVersion(DataFixTypes instance, DataFixer dataFixer, CompoundTag compoundTag, int i) {
        return NbtUtils.addCurrentDataVersion(instance.updateToCurrentVersion(dataFixer, compoundTag, i));
    }
}

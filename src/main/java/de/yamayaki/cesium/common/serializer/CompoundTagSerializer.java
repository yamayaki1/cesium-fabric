package de.yamayaki.cesium.common.serializer;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.yamayaki.cesium.api.io.IScannable;
import de.yamayaki.cesium.api.io.ISerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StreamTagVisitor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.IOException;

public class CompoundTagSerializer implements ISerializer<CompoundTag>, IScannable<StreamTagVisitor> {
    @Override
    public byte @NotNull [] serialize(final @NotNull CompoundTag input) throws IOException {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput(2048);

        NbtIo.write(input, output);

        return output.toByteArray();
    }

    @Override
    public @NotNull CompoundTag deserialize(final byte @NotNull [] input) throws IOException {
        final DataInput dataInput = ByteStreams.newDataInput(input);
        return NbtIo.read(dataInput);
    }

    @Override
    public void scan(final byte @NotNull [] input, final StreamTagVisitor scanner) throws IOException {
        final DataInput dataInput = ByteStreams.newDataInput(input);
        NbtIo.parse(dataInput, scanner, NbtAccounter.unlimitedHeap());
    }
}

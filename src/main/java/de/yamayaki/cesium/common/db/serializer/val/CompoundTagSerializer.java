package de.yamayaki.cesium.common.db.serializer.val;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.yamayaki.cesium.common.db.serializer.ValueSerializer;
import de.yamayaki.cesium.common.io.Scannable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StreamTagVisitor;

import java.io.DataInput;
import java.io.IOException;

public class CompoundTagSerializer implements ValueSerializer<CompoundTag>, Scannable<StreamTagVisitor> {
    @Override
    public byte[] serialize(final CompoundTag value) {
        try {
            final ByteArrayDataOutput output = ByteStreams.newDataOutput(2048);

            NbtIo.write(value, output);

            return output.toByteArray();
        } catch (final Throwable t) {
            throw new RuntimeException("Failed to serialize NBT", t);
        }
    }

    @Override
    public CompoundTag deserialize(final byte[] array) throws IOException {
        final DataInput dataInput = ByteStreams.newDataInput(array);
        return NbtIo.read(dataInput);
    }

    @Override
    public void scan(final byte[] array, final StreamTagVisitor scanner) throws IOException {
        final DataInput dataInput = ByteStreams.newDataInput(array);
        NbtIo.parse(dataInput, scanner, NbtAccounter.unlimitedHeap());
    }
}

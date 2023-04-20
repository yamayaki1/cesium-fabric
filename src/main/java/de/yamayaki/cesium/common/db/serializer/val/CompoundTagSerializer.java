package de.yamayaki.cesium.common.db.serializer.val;

import de.yamayaki.cesium.common.Scannable;
import de.yamayaki.cesium.common.db.serializer.ValueSerializer;
import de.yamayaki.cesium.common.io.ByteBufferInputStream;
import de.yamayaki.cesium.common.io.ByteBufferOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StreamTagVisitor;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CompoundTagSerializer implements ValueSerializer<CompoundTag>, Scannable<StreamTagVisitor> {
    @Override
    public ByteBuffer serialize(CompoundTag value) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(2048);

        try (DataOutputStream out = new DataOutputStream(bytes)) {
            NbtIo.write(value, out);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize NBT", e);
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(bytes.size());
        bytes.writeTo(new ByteBufferOutputStream(buf));
        buf.flip();

        return buf;
    }

    @Override
    public CompoundTag deserialize(ByteBuffer input) throws IOException {
        try (DataInputStream dataInput = new DataInputStream(new ByteBufferInputStream(input))) {
            return NbtIo.read(dataInput);
        }
    }

    @Override
    public void scan(ByteBuffer byteBuffer, StreamTagVisitor scanner) throws IOException {
        try (DataInputStream dataInput = new DataInputStream(new ByteBufferInputStream(byteBuffer))) {
            NbtIo.parse(dataInput, scanner);
        }
    }
}

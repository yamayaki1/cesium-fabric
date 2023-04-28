package de.yamayaki.cesium.common.db.serializer.val;

import de.yamayaki.cesium.common.Scannable;
import de.yamayaki.cesium.common.db.serializer.ValueSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StreamTagVisitor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CompoundTagSerializer implements ValueSerializer<CompoundTag>, Scannable<StreamTagVisitor> {
    @Override
    public byte[] serialize(CompoundTag value) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(2048);

        try (DataOutputStream out = new DataOutputStream(bytes)) {
            NbtIo.write(value, out);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize NBT", e);
        }

        return bytes.toByteArray();
    }

    @Override
    public CompoundTag deserialize(byte[] input) throws IOException {
        try (DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(input))) {
            return NbtIo.read(dataInput);
        }
    }

    @Override
    public void scan(byte[] byteBuffer, StreamTagVisitor scanner) throws IOException {
        try (DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(byteBuffer))) {
            NbtIo.parse(dataInput, scanner);
        }
    }
}

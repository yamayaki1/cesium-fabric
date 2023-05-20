package de.yamayaki.cesium.common.db.serializer.key;

import de.yamayaki.cesium.common.db.serializer.KeySerializer;
import net.minecraft.world.level.ChunkPos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChunkPosSerializer implements KeySerializer<ChunkPos> {
    @Override
    public byte[] serializeKey(ChunkPos value) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(8); DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeInt(value.x);
            out.writeInt(value.z);

            return bytes.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize ChunkPos", e);
        }
    }

    @Override
    public ChunkPos deserializeKey(byte[] array) {
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(array); DataInputStream in = new DataInputStream(bytes)) {
            return new ChunkPos(in.readInt(), in.readInt());
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize ChunkPos", e);
        }
    }
}

package de.yamayaki.cesium.common.db.serializer.key;

import de.yamayaki.cesium.common.db.serializer.KeySerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class UUIDSerializer implements KeySerializer<UUID> {
    @Override
    public byte[] serializeKey(UUID value) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(16); DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeLong(value.getLeastSignificantBits());
            out.writeLong(value.getMostSignificantBits());

            return bytes.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize UUID", e);
        }
    }

    @Override
    public UUID deserializeKey(byte[] array) {
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(array); DataInputStream in = new DataInputStream(bytes)) {
            final long least = in.readLong();
            final long most = in.readLong();

            return new UUID(most, least);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize UUID", e);
        }
    }
}

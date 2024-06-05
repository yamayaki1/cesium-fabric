package de.yamayaki.cesium.common.db.serializer.key;

import de.yamayaki.cesium.common.db.serializer.KeySerializer;

import java.util.UUID;

public class UUIDSerializer implements KeySerializer<UUID> {
    @Override
    public byte[] serializeKey(final UUID value) {
        final byte[] array = new byte[16];

        long least = value.getLeastSignificantBits();
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (least & 0xffL);
            least >>= 8;
        }

        long most = value.getMostSignificantBits();
        for (int i = 15; i >= 8; i--) {
            array[i] = (byte) (most & 0xffL);
            most >>= 8;
        }

        return array;
    }

    @Override
    public UUID deserializeKey(final byte[] array) {
        final long least = (array[0] & 0xFFL) << 56 | (array[1] & 0xFFL) << 48 | (array[2] & 0xFFL) << 40 | (array[3] & 0xFFL) << 32 | (array[4] & 0xFFL) << 24 | (array[5] & 0xFFL) << 16 | (array[6] & 0xFFL) << 8 | (array[7] & 0xFFL);
        final long most = (array[8] & 0xFFL) << 56 | (array[9] & 0xFFL) << 48 | (array[10] & 0xFFL) << 40 | (array[11] & 0xFFL) << 32 | (array[12] & 0xFFL) << 24 | (array[13] & 0xFFL) << 16 | (array[14] & 0xFFL) << 8 | (array[15] & 0xFFL);

        return new UUID(most, least);
    }
}

package de.yamayaki.cesium.common.db.serializer.val;

import de.yamayaki.cesium.common.db.serializer.ValueSerializer;

import java.nio.charset.StandardCharsets;

public class StringSerializer implements ValueSerializer<String> {
    @Override
    public byte[] serialize(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String deserialize(byte[] input) {
        return new String(input, StandardCharsets.UTF_8);
    }
}

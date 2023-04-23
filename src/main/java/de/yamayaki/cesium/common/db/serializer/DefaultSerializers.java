package de.yamayaki.cesium.common.db.serializer;

import de.yamayaki.cesium.common.db.serializer.key.ChunkPosSerializer;
import de.yamayaki.cesium.common.db.serializer.key.ChunkSectionPosSerializer;
import de.yamayaki.cesium.common.db.serializer.key.UUIDSerializer;
import de.yamayaki.cesium.common.db.serializer.val.CompoundTagSerializer;
import de.yamayaki.cesium.common.db.serializer.val.StringSerializer;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public class DefaultSerializers {
    private static final Reference2ReferenceMap<Class<?>, KeySerializer<?>> keySerializers = new Reference2ReferenceOpenHashMap<>();
    private static final Reference2ReferenceMap<Class<?>, ValueSerializer<?>> valueSerializers = new Reference2ReferenceOpenHashMap<>();

    static {
        keySerializers.put(UUID.class, new UUIDSerializer());
        keySerializers.put(SectionPos.class, new ChunkSectionPosSerializer());
        keySerializers.put(ChunkPos.class, new ChunkPosSerializer());

        valueSerializers.put(CompoundTag.class, new CompoundTagSerializer());
        valueSerializers.put(String.class, new StringSerializer());
    }

    @SuppressWarnings("unchecked")
    public static <K> KeySerializer<K> getKeySerializer(Class<K> clazz) {
        KeySerializer<?> serializer = keySerializers.get(clazz);

        if (serializer == null) {
            throw new NullPointerException("No serializer exists for type: " + clazz.getName());
        }

        return (KeySerializer<K>) serializer;
    }

    @SuppressWarnings("unchecked")
    public static <K> ValueSerializer<K> getValueSerializer(Class<K> clazz) {
        ValueSerializer<?> serializer = valueSerializers.get(clazz);

        if (serializer == null) {
            throw new NullPointerException("No serializer exists for type: " + clazz.getName());
        }

        return (ValueSerializer<K>) serializer;
    }
}

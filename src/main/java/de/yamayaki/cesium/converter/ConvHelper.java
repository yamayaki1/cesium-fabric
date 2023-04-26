package de.yamayaki.cesium.converter;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import de.yamayaki.cesium.CesiumMod;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ConvHelper {
    public static List<File> resolveAllEnding(final Path path, final String ending) {
        final File directory = path.toFile();
        final File[] files = directory.listFiles((dir, name) -> name.endsWith(ending));

        return files != null ? Arrays.stream(files).toList() : ImmutableList.of();
    }

    public static String getContents(final Path path) throws IOException {
        final byte[] bytes = Files.toByteArray(path.toFile());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void saveToFile(final Path path, final String string) throws IOException {
        FileUtils.writeStringToFile(path.toFile(), string, StandardCharsets.UTF_8);
    }

    public static void transferPlayerData(final IPlayerStorage originalStorage, final IPlayerStorage newStorage) {
        for (UUID player : originalStorage.getAllPlayers()) {
            CesiumMod.logger().info("Transfering player {}", player);

            newStorage.setPlayerNBT(player, originalStorage.getPlayerNBT(player));
            newStorage.setPlayerAdvancements(player, originalStorage.getPlayerAdvancements(player));
            newStorage.setPlayerStatistics(player, originalStorage.getPlayerStatistics(player));
        }
    }

    public static void transferChunkData(final IChunkStorage originalStorage, final IChunkStorage newStorage) {
        final List<ChunkPos> chunkList = originalStorage.getAllChunks();
        CesiumMod.logger().info("Transfering {} chunks", chunkList.size());

        for (int i = 0; i < chunkList.size(); i++) {
            final ChunkPos chunkPos = chunkList.get(i);

            newStorage.setChunkData(chunkPos, originalStorage.getChunkData(chunkPos));
            newStorage.setPOIData(chunkPos, originalStorage.getPOIData(chunkPos));
            newStorage.setEntityData(chunkPos, originalStorage.getEntityData(chunkPos));

            if (i % 10240 == 0) {
                CesiumMod.logger().info("Transferred chunk {}, flushing data", i);
                newStorage.flush();
            }
        }

        newStorage.flush();
    }
}

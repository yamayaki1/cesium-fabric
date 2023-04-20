package me.jellysquid.mods.radon.converter;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ConvHelper {
    public static List<File> resolveAllEnding(Path path, String ending) {
        File directory = path.toFile();
        File[] files = directory.listFiles((dir, name) -> name.endsWith(ending));

        return files != null ? Arrays.stream(files).toList() : ImmutableList.of();
    }

    public static String getContents(Path path) throws IOException {
        byte[] bytes = Files.toByteArray(path.toFile());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void saveToFile(Path path, String string) throws IOException {
        FileUtils.writeStringToFile(path.toFile(), string, StandardCharsets.UTF_8);
    }

    public static void transferPlayerData(Logger logger, IPlayerStorage originalStorage, IPlayerStorage newStorage) {
        for (UUID player : originalStorage.getAllPlayers()) {
            logger.info("Transfering player {}", player);

            newStorage.setPlayerNBT(player, originalStorage.getPlayerNBT(player));
            newStorage.setPlayerAdvancements(player, originalStorage.getPlayerAdvancements(player));
            newStorage.setPlayerStatistics(player, originalStorage.getPlayerStatistics(player));
        }
    }

    public static void transferChunkData(Logger logger, IChunkStorage originalStorage, IChunkStorage newStorage) {
        final List<ChunkPos> chunkList = originalStorage.getAllChunks();
        logger.info("Transfering {} chunks", chunkList.size());

        for (int i = 0; i < chunkList.size(); i++) {
            ChunkPos chunkPos = chunkList.get(i);

            newStorage.setChunkData(chunkPos, originalStorage.getChunkData(chunkPos));
            newStorage.setPOIData(chunkPos, originalStorage.getPOIData(chunkPos));
            newStorage.setEntityData(chunkPos, originalStorage.getEntityData(chunkPos));

            if(i % 10240 == 0) {
                logger.info("Transferred chunk {}, flushing data", i);
                newStorage.flush();
            }
        }

        newStorage.flush();
    }
}

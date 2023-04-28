package de.yamayaki.cesium.converter;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

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
}

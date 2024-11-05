package de.yamayaki.cesium;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FileHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void ensureDirectory(final @NotNull Path path) {
        try {
            if (!Files.isDirectory(path)) Files.createDirectories(path);
        } catch (final IOException i) {
            throw new RuntimeException("Failed to create directory.", i);
        }
    }

    public static <T> Stream<T> traverseFilesSafe(final @NotNull Supplier<String> onError, final @NotNull Path searchPath, final @NotNull PathMapper<T> pathMapper, final @NotNull Rules... rules) {
        try {
            return traverseFiles(searchPath, pathMapper, rules);
        } catch (final IOException i) {
            throw new RuntimeException(onError.get(), i);
        }
    }

    @SuppressWarnings("resource")
    public static <T> Stream<T> traverseFiles(final @NotNull Path searchPath, final @NotNull PathMapper<T> pathMapper, final @NotNull Rules... rules) throws IOException {
        if (!Files.isDirectory(searchPath)) {
            return Stream.empty();
        }

        return Files.walk(searchPath, 1)
                .filter(path -> __matchesAllOf(path, rules))
                .map(pathMapper::mapTo);
    }

    private static boolean __matchesAllOf(final @NotNull Path path, final @NotNull Rules... rules) {
        final String pathString = path.getFileName().toString();

        for (final Rules rule : rules) {
            if (!rule.matches(pathString)) {
                LOGGER.warn("Found non complying file in directory, ignoring ({}). (Failed {})", pathString, rule.name());
                return false;
            }
        }

        return true;
    }

    public abstract static class PathMapper<T> {
        abstract @NotNull T mapTo(final @NotNull Path path);
    }

    public static class NoOpMapper extends PathMapper<Path> {
        @Override
        @NotNull
        Path mapTo(final @NotNull Path path) {
            return path;
        }
    }

    public static class UUIDMapper extends PathMapper<UUID> {
        @Override
        @NotNull
        UUID mapTo(final @NotNull Path path) {
            String pathString = path.getFileName().toString();

            if (pathString.contains(".")) {
                pathString = pathString.substring(0, pathString.lastIndexOf('.'));
            }

            return UUID.fromString(pathString);
        }
    }

    public abstract static class Rules {
        abstract boolean matches(final @NotNull String pathString);

        abstract String name();
    }

    public static class Or extends Rules {
        private final Rules a;
        private final Rules b;

        public Or(final Rules a, final Rules b) {
            this.a = a;
            this.b = b;
        }

        @Override
        boolean matches(final @NotNull String pathString) {
            return this.a.matches(pathString) || this.b.matches(pathString);
        }

        @Override
        String name() {
            return this.a.name() + " or " + this.b.name();
        }
    }

    public static class FileExtensionRule extends Rules {
        private final String endsWith;

        public FileExtensionRule(final @NotNull String endsWith) {
            this.endsWith = endsWith;
        }

        @Override
        boolean matches(final @NotNull String pathString) {
            return pathString.endsWith(endsWith);
        }

        @Override
        String name() {
            return "file extension " + this.endsWith;
        }
    }

    public static class PatternRule extends Rules {
        private final Pattern pattern;

        public PatternRule(final @NotNull Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        boolean matches(final @NotNull String pathString) {
            final Matcher matcher = pattern.matcher(pathString);
            return matcher.matches();
        }

        @Override
        String name() {
            return "pattern " + this.pattern.pattern();
        }
    }
}

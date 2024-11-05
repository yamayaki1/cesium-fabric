package de.yamayaki.cesium.storage.impl;

import de.yamayaki.cesium.FileHelper;
import de.yamayaki.cesium.storage.IComponentStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileComponent<T> implements IComponentStorage<UUID, T> {
    private static final Pattern UUID_REG = Pattern.compile("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\\.(dat|json)$");

    private final Path basePath;
    private final String extension;
    private final boolean compressed;

    public FileComponent(final Path basePath, final String extension, final boolean compressed) {
        this.basePath = basePath;
        this.extension = extension;
        this.compressed = compressed;
    }

    @Override
    public @Nullable T getValue(final @NotNull UUID key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte @Nullable [] getRaw(final @NotNull UUID key) {
        final Path filePath = this.basePath.resolve(key + this.extension);

        try {
            if (!Files.exists(filePath)) {
                return null;
            }

            final InputStream fileStream = Files.newInputStream(filePath);

            try {
                final InputStream zipped = this.compressed ? new GZIPInputStream(fileStream) : fileStream;

                try (final DataInputStream inputStream = new DataInputStream(zipped)) {
                    return inputStream.readAllBytes();
                }
            } catch (final IOException i) {
                fileStream.close();
                throw i;
            }
        } catch (final IOException i) {
            throw new RuntimeException("Could not open file " + filePath, i);
        }
    }

    @Override
    public void putValue(final @NotNull UUID key, final @Nullable T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putRaw(final @NotNull UUID key, final byte @Nullable [] value) {
        final Path filePath = this.basePath.resolve(key + this.extension);

        try {
            if (value == null) {
                Files.deleteIfExists(filePath);
            } else {
                FileHelper.ensureDirectory(filePath.getParent());
                final OutputStream fileStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE);

                try {
                    final OutputStream zipped = this.compressed ? new GZIPOutputStream(fileStream) : fileStream;

                    try (final DataOutputStream inputStream = new DataOutputStream(zipped)) {
                        inputStream.write(value);
                    }
                } catch (final IOException i) {
                    fileStream.close();
                    throw i;
                }
            }
        } catch (final IOException i) {
            throw new RuntimeException("Could not open file " + filePath, i);
        }
    }

    @Override
    public <S> void scan(final @NotNull UUID key, final @NotNull S scanner) {
        // Not supported
    }

    @Override
    public @NotNull List<UUID> allKeys() {
        return FileHelper.traverseFilesSafe(
                () -> "Could not resolve players from directory, aborting.",
                this.basePath,
                new FileHelper.UUIDMapper(),
                new FileHelper.FileExtensionRule(this.extension),
                new FileHelper.PatternRule(UUID_REG)
        ).toList();
    }
}

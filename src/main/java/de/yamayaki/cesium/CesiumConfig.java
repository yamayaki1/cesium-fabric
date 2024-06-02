package de.yamayaki.cesium;

public class CesiumConfig {
    private final Option<Boolean> log_map_grows = new Option<>(false, "Log when a database map is being resized.");
    private final Option<Boolean> show_debug_info = new Option<>(false, "Display information on the debug screen.");
    private final Option<Integer> zstd_compression_level = new Option<>(12, "The zstd library supports compression levels from 1 to 22. The lower the level, the faster the speed (at the cost of compression).");
    private final Option<Boolean> zstd_use_dictionary = new Option<>(true, "The compression ratio achievable can be highly improved using the built-in dictionary.");
    private final Option<Boolean> disable_compression = new Option<>(false, "HERE BE DRAGONS!! Forcefully disable all compression. A different database is used for uncompressed data, you can't have compressed and uncompressed worlds at the same time. Compressed worlds have to be reimported.");
    private final Option<Boolean> force_save_after_tick = new Option<>(false, "HERE BE DRAGONS!! Forces the (internal) to save all player and chunk data after each server tick. This option can slow down your server.");

    public boolean logMapGrows() {
        return this.log_map_grows.value;
    }

    public boolean showDebugInfo() {
        return this.show_debug_info.value;
    }

    public int compressionLevel() {
        return this.zstd_compression_level.value;
    }

    public boolean useDictionary() {
        return this.zstd_use_dictionary.value;
    }

    public boolean isUncompressed() {
        return this.disable_compression.value;
    }

    public boolean saveAfterTick() {
        return this.force_save_after_tick.value;
    }

    @SuppressWarnings({"unused", "FieldMayBeFinal", "FieldCanBeLocal"})
    private static class Option<T> {
        private T value;
        private final String comment;

        public Option(T value, String comment) {
            this.value = value;
            this.comment = comment;
        }
    }
}

package de.yamayaki.cesium.common;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class Config {
    private Client client = new Client();
    private Compression compression = new Compression();
    private MapGrow mapGrow = new MapGrow();

    public Client getClient() {
        return this.client;
    }

    public Compression getCompression() {
        return this.compression;
    }

    public MapGrow getMapGrow() {
        return this.mapGrow;
    }

    public static class Client {
        private boolean show_debug = false;

        public boolean showDebug() {
            return this.show_debug;
        }
    }

    public static class Compression {
        private int level = 9;
        private boolean use_dictionary = false;

        public int getLevel() {
            return this.level;
        }

        public boolean usesDictionary() {
            return this.use_dictionary;
        }
    }

    public static class MapGrow {
        private boolean log = false;
        private float multiply = 1.0F;

        public boolean getLog() {
            return this.log;
        }

        public float getMultiply() {
            return this.multiply;
        }
    }
}

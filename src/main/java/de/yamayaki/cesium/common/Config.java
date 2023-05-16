package de.yamayaki.cesium.common;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class Config {
    private Compression compression = new Compression();
    private MapGrow mapGrow = new MapGrow();

    public Compression getCompression() {
        return this.compression;
    }

    public MapGrow getMapGrow() {
        return this.mapGrow;
    }

    public static class Compression {
        private int level = 12;

        public int getLevel() {
            return this.level;
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

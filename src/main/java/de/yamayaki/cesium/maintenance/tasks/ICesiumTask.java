package de.yamayaki.cesium.maintenance.tasks;

public interface ICesiumTask {
    void cancelTask();

    boolean running();

    String levelName();

    String status();

    int totalElements();

    int currentElement();

    double percentage();

    enum CesiumTask {
        TO_ANVIL,
        TO_CESIUM,
        COMPACT
    }
}

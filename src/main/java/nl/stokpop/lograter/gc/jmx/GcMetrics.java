package nl.stokpop.lograter.gc.jmx;

public interface GcMetrics {
    long getTimestamp();

    long getHeapMemoryUsedBytes();

    long getOldGenerationUsedBytes();

    double getGcDurationMs();

    long getYoungGenerationGcTime();

    long getOldGenerationGcTime();
}

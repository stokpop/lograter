package nl.stokpop.lograter.jmx.memory;

public interface MemoryMetrics {
    long getTimestamp();

    long getHeapMemoryUsedBytes();

    long getEdenUsedBytes();

    long getSurvivorUsedBytes();

    long getTenuredUsedBytes();

    default long getYoungGenerationUsedBytes() {
        return getEdenUsedBytes() + getSurvivorUsedBytes();
    }

    long getOldGenerationUsedBytes();

    long getMetaSpaceUsedBytes();

    long getCompressedClassSpaceUsedBytes();

    long getCodeCacheUsedBytes();

    double getGcDurationMs();

    long getYoungGenerationGcTime();

    long getOldGenerationGcTime();
}

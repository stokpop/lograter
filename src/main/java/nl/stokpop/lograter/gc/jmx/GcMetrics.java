package nl.stokpop.lograter.gc.jmx;

import nl.stokpop.lograter.gc.GcReason;
import nl.stokpop.lograter.gc.GcType;

public interface GcMetrics {
    default int getId() {
        return 0;
    }

    long getTimestamp();

    long getHeapMemoryUsedBytes();

    long getOldGenerationUsedBytes();

    double getGcDurationMs();

    default GcType getType() {
        return null; /*GcType.NONE;*/
    }

    default GcReason getGcReason() {
        return null; /*GcReason.NONE;*/ }

    default String getSysGcReason() { return ""; }

    default long getYoungGenerationGcTime() {
        return -1;
    }

    default long getOldGenerationGcTime() {
        return -1;
    }


}

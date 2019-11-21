package nl.stokpop.lograter.gc.jmx;

import nl.stokpop.lograter.util.time.TimePeriod;

import java.util.List;

public enum HeapUsageResult {
    INSTANCE;

    public double calculateGcOverheadPercentage(List<GcMetrics> memoryMetrics, TimePeriod timePeriod) {
        double totalGcDuration = memoryMetrics
                .stream()
                .filter(metrics -> timePeriod.isWithinTimePeriod(metrics.getTimestamp()))
                .map(GcMetrics::getGcDurationMs)
                .mapToDouble(Double::doubleValue)
                .sum();

        return (totalGcDuration / timePeriod.getDurationInMillis()) * 100;
    }
}
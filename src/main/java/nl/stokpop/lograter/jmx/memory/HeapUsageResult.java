package nl.stokpop.lograter.jmx.memory;

import nl.stokpop.lograter.util.time.TimePeriod;

import java.util.List;

public enum HeapUsageResult {
    INSTANCE;

    public double calculateGcOverheadPercentage(List<MemoryMetrics> memoryMetrics, TimePeriod timePeriod) {
        double totalGcDuration = memoryMetrics
                .stream()
                .filter(metrics -> timePeriod.isWithinTimePeriod(metrics.getTimestamp()))
                .map(MemoryMetrics::getGcDurationMs)
                .mapToDouble(Double::doubleValue)
                .sum();

        return (totalGcDuration / timePeriod.getDurationInMillis()) * 100;
    }
}
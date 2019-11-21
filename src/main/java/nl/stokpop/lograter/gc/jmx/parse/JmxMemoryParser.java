package nl.stokpop.lograter.gc.jmx.parse;

import nl.stokpop.lograter.gc.jmx.GcMetrics;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum JmxMemoryParser {
    INSTANCE;

    public List<GcMetrics> getMemoryMetricsEntriesFromFile(File file, TimePeriod timePeriod) {
        Stream<GcMetrics> jMxMemoryMetrics = CsvFileParser.INSTANCE
                .parse(file)
                .filter(memoryMetrics -> timePeriod.isWithinTimePeriod(memoryMetrics.getTimestamp()));

        return jMxMemoryMetrics.collect(Collectors.toList());
    }

    public List<GcMetrics> getMemoryMetricsEntriesFromFiles(List<File> files, TimePeriod filterPeriod) {
        List<GcMetrics> jMxMemoryMetrics = new ArrayList<>();

        for (File file : files) {
            jMxMemoryMetrics.addAll(getMemoryMetricsEntriesFromFile(file, filterPeriod));
        }
        orderOnTimestamp(jMxMemoryMetrics);
        return jMxMemoryMetrics;
    }

    private void orderOnTimestamp(List<GcMetrics> jMxMemoryMetrics) {
        jMxMemoryMetrics.sort((o1, o2) -> (int) (o1.getTimestamp() - o2.getTimestamp()));
    }
}
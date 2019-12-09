package nl.stokpop.lograter.gc.jmx.parse;

import nl.stokpop.lograter.gc.jmx.MemoryMetrics;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum JmxMemoryParser {
    INSTANCE;

    public List<MemoryMetrics> getMemoryMetricsEntriesFromFile(File file, TimePeriod timePeriod) {
        Stream<MemoryMetrics> jMxMemoryMetrics = CsvFileParser.INSTANCE
                .parse(file)
                .filter(memoryMetrics -> timePeriod.isWithinTimePeriod(memoryMetrics.getTimestamp()));

        return jMxMemoryMetrics.collect(Collectors.toList());
    }

    public List<MemoryMetrics> getMemoryMetricsEntriesFromFiles(List<File> files, TimePeriod filterPeriod) {
        List<MemoryMetrics> jMxMemoryMetrics = new ArrayList<>();

        for (File file : files) {
            jMxMemoryMetrics.addAll(getMemoryMetricsEntriesFromFile(file, filterPeriod));
        }
        orderOnTimestamp(jMxMemoryMetrics);
        return jMxMemoryMetrics;
    }

    private void orderOnTimestamp(List<MemoryMetrics> jMxMemoryMetrics) {
        jMxMemoryMetrics.sort((o1, o2) -> (int) (o1.getTimestamp() - o2.getTimestamp()));
    }
}
package nl.stokpop.lograter.jmx.os.parse;

import nl.stokpop.lograter.jmx.os.JmxOperatingSystemMetrics;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum JmxOperatingSystemParser {
    INSTANCE;

    public List<JmxOperatingSystemMetrics> getOperatingSystemEntriesFromFile(File file, TimePeriod timePeriod) {
        Stream<JmxOperatingSystemMetrics> jmxOperatingSystemMetrics = JmxOperatingSystemCsvFileParser.INSTANCE
                .parse(file)
                .filter(operatingSystemMetrics -> timePeriod.isWithinTimePeriod(operatingSystemMetrics.getTimestamp()));

        return jmxOperatingSystemMetrics.collect(Collectors.toList());
    }

    public List<JmxOperatingSystemMetrics> getOperatingSystemMetricsEntriesFromFiles(List<File> files, TimePeriod filterPeriod) {
        List<JmxOperatingSystemMetrics> jmxOperatingSystemMetrics = new ArrayList<>();

        for (File file : files) {
            jmxOperatingSystemMetrics.addAll(getOperatingSystemEntriesFromFile(file, filterPeriod));
        }
        orderOnTimestamp(jmxOperatingSystemMetrics);
        return jmxOperatingSystemMetrics;
    }

    private void orderOnTimestamp(List<JmxOperatingSystemMetrics> jmxOperatingSystemMetrics) {
        jmxOperatingSystemMetrics.sort((o1, o2) -> (int) (o1.getTimestamp() - o2.getTimestamp()));
    }
}
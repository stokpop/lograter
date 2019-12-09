package nl.stokpop.lograter.gc.jmx.parse;

import nl.stokpop.lograter.gc.jmx.MemoryMetrics;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class CsvFileParserTest {
    @Test
    public void parseConcurrentMarkSweepTest() {
        List<MemoryMetrics> memoryMetrics = CsvFileParser.INSTANCE
                .parse(getFile("jmx/jvm-heap-metrics-concurrent-mark-sweep.log")).collect(Collectors.toList());

        assertEquals(5, memoryMetrics.size());
        assertEquals(14266424, memoryMetrics.get(4).getHeapMemoryUsedBytes());
        assertEquals(562416, memoryMetrics.get(4).getOldGenerationUsedBytes());
        assertEquals(0, memoryMetrics.get(4).getYoungGenerationGcTime());
        assertEquals(120, memoryMetrics.get(4).getOldGenerationGcTime());
        assertEquals(120, (long) memoryMetrics.get(4).getGcDurationMs());
        assertEquals(562416, memoryMetrics.get(4).getOldGenerationUsedBytes());
    }

    @Test
    public void parseG1Test() {
        List<MemoryMetrics> memoryMetrics = CsvFileParser.INSTANCE
                .parse(getFile("jmx/jvm-heap-metrics-G1.log")).collect(Collectors.toList());

        assertEquals(4, memoryMetrics.size());
        assertEquals(13816320, memoryMetrics.get(3).getHeapMemoryUsedBytes());
        assertEquals(3330560, memoryMetrics.get(3).getOldGenerationUsedBytes());
        assertEquals(100, memoryMetrics.get(3).getYoungGenerationGcTime());
        assertEquals(500, memoryMetrics.get(3).getOldGenerationGcTime());
        assertEquals(600, (long) memoryMetrics.get(3).getGcDurationMs());
        assertEquals(3330560, memoryMetrics.get(3).getOldGenerationUsedBytes());
    }

    @Test
    public void parseMarkSweepCompactTest() {
        List<MemoryMetrics> memoryMetrics = CsvFileParser.INSTANCE
                .parse(getFile("jmx/jvm-heap-metrics-mark-sweep-compact.log")).collect(Collectors.toList());

        assertEquals(10, memoryMetrics.size());
    }

    @Test
    public void parseMarkSweepCompactJava10Test() {
        List<MemoryMetrics> memoryMetrics = CsvFileParser.INSTANCE
                .parse(getFile("jmx/jvm-heap-metrics-mark-sweep-compact-java-10.log")).collect(Collectors.toList());

        assertEquals(10, memoryMetrics.size());
    }

    @Test
    public void parseParallelTest() {
        List<MemoryMetrics> memoryMetrics = CsvFileParser.INSTANCE
                .parse(getFile("jmx/jvm-heap-metrics-parallel.log")).collect(Collectors.toList());

        assertEquals(11, memoryMetrics.size());
    }

    private File getFile(String filePath) {
        URL resource = this.getClass().getClassLoader().getResource(filePath);
        return new File(resource.getFile());
    }
}
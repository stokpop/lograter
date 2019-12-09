package nl.stokpop.lograter.gc.jmx.parse;

import nl.stokpop.lograter.gc.jmx.MemoryMetrics;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JmxMemoryParserTest {

    @Test
    public void getMemoryMetricsEntriesFromFileTes() {
        TimePeriod timePeriod = TimePeriod.createExcludingEndTime(1572875922000L, 1572875950000L);
        List<MemoryMetrics> memoryMetrics = JmxMemoryParser.INSTANCE.getMemoryMetricsEntriesFromFile(getFile("jmx/jvm-heap-metrics-mark-sweep-compact.log"), timePeriod);

        assertEquals(9, memoryMetrics.size());
    }

    @Test
    public void getMemoryMetricsEntriesFromFilesTes() {
        TimePeriod timePeriod = TimePeriod.MAX_TIME_PERIOD;
        List<File> files = Arrays.asList(getFile("jmx/jvm-heap-metrics-mark-sweep-compact.log"), getFile("jmx/jvm-heap-metrics-G1.log"));

        List<MemoryMetrics> memoryMetrics = JmxMemoryParser.INSTANCE.getMemoryMetricsEntriesFromFiles(files, timePeriod);

        assertEquals(14, memoryMetrics.size());
    }

    private File getFile(String filePath) {
        URL resource = this.getClass().getClassLoader().getResource(filePath);
        return new File(resource.getFile());
    }
}
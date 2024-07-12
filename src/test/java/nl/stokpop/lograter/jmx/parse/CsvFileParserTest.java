/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.stokpop.lograter.jmx.parse;

import nl.stokpop.lograter.jmx.memory.MemoryMetrics;
import nl.stokpop.lograter.jmx.memory.parse.CsvFileParser;
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
        MemoryMetrics memoryMetric5 = memoryMetrics.get(4);
        assertEquals(14266424, memoryMetric5.getHeapMemoryUsedBytes());
        assertEquals(562416, memoryMetric5.getOldGenerationUsedBytes());
        assertEquals(0, memoryMetric5.getYoungGenerationGcTime());
        assertEquals(120, memoryMetric5.getOldGenerationGcTime());
        assertEquals(120, (long) memoryMetric5.getGcDurationMs());
    }

    @Test
    public void parseG1Test() {
        List<MemoryMetrics> memoryMetrics = CsvFileParser.INSTANCE
                .parse(getFile("jmx/jvm-heap-metrics-G1.log")).collect(Collectors.toList());

        assertEquals(4, memoryMetrics.size());
        MemoryMetrics memoryMetric4 = memoryMetrics.get(3);
        assertEquals(13816320, memoryMetric4.getHeapMemoryUsedBytes());
        assertEquals(3330560, memoryMetric4.getOldGenerationUsedBytes());
        assertEquals(100, memoryMetric4.getYoungGenerationGcTime());
        assertEquals(500, memoryMetric4.getOldGenerationGcTime());
        assertEquals(600, (long) memoryMetric4.getGcDurationMs());
    }

    @Test
    public void parseG1Java17Test() {
        List<MemoryMetrics> memoryMetrics = CsvFileParser.INSTANCE
                .parse(getFile("jmx/jvm-heap-metrics-G1-java-17.log")).collect(Collectors.toList());

        assertEquals(9, memoryMetrics.size());
        MemoryMetrics memoryMetric4 = memoryMetrics.get(3);
        assertEquals(246450016, memoryMetric4.getHeapMemoryUsedBytes());
        assertEquals(193232384, memoryMetric4.getOldGenerationUsedBytes());
        assertEquals(73, memoryMetric4.getYoungGenerationGcTime());
        assertEquals(0, memoryMetric4.getOldGenerationGcTime());
        assertEquals(73, (long) memoryMetric4.getGcDurationMs());
        assertEquals(70947072, memoryMetric4.getCodeCacheUsedBytes());
    }

    @Test
    public void parseG1Java21Test() {
        List<MemoryMetrics> memoryMetrics = CsvFileParser.INSTANCE
                .parse(getFile("jmx/jvm-heap-metrics-G1-java-21.log")).collect(Collectors.toList());

        assertEquals(9, memoryMetrics.size());
        MemoryMetrics memoryMetric4 = memoryMetrics.get(3);
        assertEquals(176521200, memoryMetric4.getHeapMemoryUsedBytes());
        assertEquals(125140976, memoryMetric4.getOldGenerationUsedBytes());
        assertEquals(70, memoryMetric4.getYoungGenerationGcTime());
        assertEquals(0, memoryMetric4.getOldGenerationGcTime());
        assertEquals(70, (long) memoryMetric4.getGcDurationMs());
        assertEquals(42027008, memoryMetric4.getCodeCacheUsedBytes());
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
        assert resource != null;
        return new File(resource.getFile());
    }
}
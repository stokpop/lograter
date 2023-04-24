/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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
        assert resource != null;
        return new File(resource.getFile());
    }
}
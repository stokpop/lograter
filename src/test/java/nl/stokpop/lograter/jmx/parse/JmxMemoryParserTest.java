/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
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
import nl.stokpop.lograter.jmx.memory.parse.JmxMemoryParser;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JmxMemoryParserTest {

    @Test
    public void getMemoryMetricsEntriesFromFileTest() {
        TimePeriod timePeriod = TimePeriod.createExcludingEndTime(1572875922000L, 1572875950000L);
        List<MemoryMetrics> memoryMetrics = JmxMemoryParser.INSTANCE.getMemoryMetricsEntriesFromFile(getFile("jmx/jvm-heap-metrics-mark-sweep-compact.log"), timePeriod);

        assertEquals(9, memoryMetrics.size());
    }

    @Test
    public void getMemoryMetricsEntriesFromFilesTest() {
        TimePeriod timePeriod = TimePeriod.MAX_TIME_PERIOD;
        List<File> files = Arrays.asList(getFile("jmx/jvm-heap-metrics-mark-sweep-compact.log"), getFile("jmx/jvm-heap-metrics-G1.log"));

        List<MemoryMetrics> memoryMetrics = JmxMemoryParser.INSTANCE.getMemoryMetricsEntriesFromFiles(files, timePeriod);

        assertEquals(14, memoryMetrics.size());
    }

    @Test
    public void testShenandoah() {
        TimePeriod timePeriod = TimePeriod.MAX_TIME_PERIOD;

        List<File> files = Collections.singletonList(getFile("jmx/gc_shenandoah.log"));

        List<MemoryMetrics> memoryMetrics = JmxMemoryParser.INSTANCE.getMemoryMetricsEntriesFromFiles(files, timePeriod);

        assertEquals(1019, memoryMetrics.size());
        assertTrue("all low gc times 😀", memoryMetrics.stream().allMatch(m -> m.getGcDurationMs() < 300));
    }

    private File getFile(String filePath) {
        URL resource = this.getClass().getClassLoader().getResource(filePath);
        assert resource != null;
        return new File(resource.getFile());
    }
}
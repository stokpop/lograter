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
package nl.stokpop.lograter.jmx.memory.parse;

import nl.stokpop.lograter.jmx.memory.MemoryMetrics;
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
/*
 * Copyright (C) 2020 Peter Paul Bakker, Stokpop Software Solutions
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
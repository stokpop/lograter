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
package nl.stokpop.lograter.jmx.memory;

import nl.stokpop.lograter.util.time.TimePeriod;

import java.util.List;

public enum HeapUsageResult {
    INSTANCE;

    public double calculateGcOverheadPercentage(List<MemoryMetrics> memoryMetrics, TimePeriod timePeriod) {
        double totalGcDuration = memoryMetrics
                .stream()
                .filter(metrics -> timePeriod.isWithinTimePeriod(metrics.getTimestamp()))
                .map(MemoryMetrics::getGcDurationMs)
                .mapToDouble(Double::doubleValue)
                .sum();

        return (totalGcDuration / timePeriod.getDurationInMillis()) * 100;
    }
}
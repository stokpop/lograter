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
package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.logentry.LatencyLogEntry;
import nl.stokpop.lograter.processor.CounterKeyCreator;
import nl.stokpop.lograter.util.linemapper.LineMap;

import java.util.List;

public class LatencyCounterKeyCreator implements CounterKeyCreator<LatencyLogEntry> {

    private final List<String> groupByFields;

    public LatencyCounterKeyCreator(final List<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    @Override
    public final CounterKey createCounterKey(final LatencyLogEntry entry) {
        return createCounterKey(entry, counterKeyBaseName(entry));
    }

    @Override
    public final CounterKey createCounterKey(final LatencyLogEntry entry, final LineMap lineMap) {
        return createCounterKey(entry, counterKeyBaseName(entry, lineMap));
    }

    @Override
    public final CounterKey createCounterKey(final LatencyLogEntry entry, final String baseName) {
        return constructCounterKey(entry, baseName, groupByFields);
    }

    public String counterKeyBaseName(LatencyLogEntry entry) {
        return entry.getMessage();
    }

    private String counterKeyBaseName(LatencyLogEntry entry, LineMap lineMap) {
        return lineMap.getNameWithReplacementsFromLine(entry.getMessage());
    }

}
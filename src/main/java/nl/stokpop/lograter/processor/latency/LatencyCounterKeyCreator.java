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
package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.logentry.LatencyLogEntry;
import nl.stokpop.lograter.processor.CounterKeyCreator;
import nl.stokpop.lograter.util.linemapper.LineMap;

import java.util.List;

public class LatencyCounterKeyCreator implements CounterKeyCreator<LatencyLogEntry> {

    private static final char SEP_CHAR = ',';

    private final List<String> groupByFields;

    public LatencyCounterKeyCreator(final List<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    @Override
    public final CounterKey createCounterKey(final LatencyLogEntry logEntry) {
        StringBuilder key = new StringBuilder(counterKeyBaseName(logEntry));
        addCounterFields(logEntry, key);
        return CounterKey.of(key.toString());
    }

    @Override
    public final CounterKey createCounterKey(final LatencyLogEntry logEntry, final LineMap lineMap) {
        StringBuilder key = new StringBuilder(counterKeyBaseName(logEntry, lineMap));
        addCounterFields(logEntry, key);
        return CounterKey.of(key.toString());
    }

    @Override
    public final CounterKey createCounterKey(final LatencyLogEntry logEntry, final String baseName) {
        StringBuilder key = new StringBuilder(baseName);
        addCounterFields(logEntry, key);
        return CounterKey.of(key.toString());
    }

    private void addCounterFields(LatencyLogEntry logEntry, StringBuilder key) {
        if (!groupByFields.isEmpty()) {
            for (String groupByField : groupByFields) {
                String field = logEntry.getField(groupByField);
                String sanitizedField = field.replace(",", "_");
                key.append(SEP_CHAR).append(sanitizedField);
            }
        }
    }

    public String counterKeyBaseName(LatencyLogEntry entry) {
        return entry.getMessage();
    }

    private String counterKeyBaseName(LatencyLogEntry entry, LineMap lineMap) {
        return lineMap.getNameWithReplacementsFromLine(entry.getMessage());
    }

}
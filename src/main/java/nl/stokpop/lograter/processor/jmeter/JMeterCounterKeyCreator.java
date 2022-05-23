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
package nl.stokpop.lograter.processor.jmeter;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.processor.CounterKeyCreator;
import nl.stokpop.lograter.util.linemapper.LineMap;

import java.util.Collections;
import java.util.List;

public class JMeterCounterKeyCreator implements CounterKeyCreator<JMeterLogEntry> {

    private static final char SEP_CHAR = ',';

    private final boolean filterHttpStatus;
    private final List<String> groupByFields;

    public JMeterCounterKeyCreator(boolean filterHttpStatus) {
        this(filterHttpStatus, Collections.emptyList());
    }

    public JMeterCounterKeyCreator(final boolean filterHttpStatus, final List<String> groupByFields) {
        this.filterHttpStatus = filterHttpStatus;
        this.groupByFields = groupByFields;
    }

    @Override
    public final CounterKey createCounterKey(final JMeterLogEntry logEntry) {
        StringBuilder key = new StringBuilder(counterKeyBaseName(logEntry));
        addHttpMethodAndStatusAndFields(logEntry, key);
        return CounterKey.of(key.toString());
    }

    @Override
    public final CounterKey createCounterKey(final JMeterLogEntry logEntry, final LineMap lineMap) {
        StringBuilder key = new StringBuilder(counterKeyBaseName(logEntry, lineMap));
        addHttpMethodAndStatusAndFields(logEntry, key);
        return CounterKey.of(key.toString());
    }

    @Override
    public final CounterKey createCounterKey(final JMeterLogEntry logEntry, final String baseName) {
        StringBuilder key = new StringBuilder(baseName);
        addHttpMethodAndStatusAndFields(logEntry, key);
        return CounterKey.of(key.toString());
    }

    private void addHttpMethodAndStatusAndFields(JMeterLogEntry logEntry, StringBuilder key) {
        if (filterHttpStatus) key.append(SEP_CHAR).append(logEntry.getCode());
        if (!groupByFields.isEmpty()) {
            for (String groupByField : groupByFields) {
                String field = logEntry.getField(groupByField);
                String sanitizedField = field.replace(",", "_");
                key.append(SEP_CHAR).append(sanitizedField);
            }
        }
    }

    public String counterKeyBaseName(JMeterLogEntry entry) {
        return entry.getUrl();
    }

    private String counterKeyBaseName(JMeterLogEntry entry, LineMap lineMap) {
        return lineMap.getNameWithReplacementsFromLine(entry.getUrl());
    }

}
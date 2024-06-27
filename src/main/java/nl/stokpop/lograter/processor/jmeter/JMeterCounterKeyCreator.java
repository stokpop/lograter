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
package nl.stokpop.lograter.processor.jmeter;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.processor.CounterKeyCreator;
import nl.stokpop.lograter.util.linemapper.LineMap;

import java.util.Collections;
import java.util.List;

public class JMeterCounterKeyCreator implements CounterKeyCreator<JMeterLogEntry> {

    private final List<String> groupByFields;

    public JMeterCounterKeyCreator() {
        this(Collections.emptyList());
    }

    public JMeterCounterKeyCreator(final List<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    @Override
    public final CounterKey createCounterKey(final JMeterLogEntry entry) {
        return createCounterKey(entry, counterKeyBaseName(entry));
    }

    @Override
    public final CounterKey createCounterKey(final JMeterLogEntry entry, final LineMap lineMap) {
        return createCounterKey(entry, counterKeyBaseName(entry, lineMap));
    }

    @Override
    public final CounterKey createCounterKey(final JMeterLogEntry entry, final String baseName) {
        return constructCounterKey(entry, baseName, groupByFields);
    }

    public String counterKeyBaseName(JMeterLogEntry entry) {
        return entry.getUrl();
    }

    private String counterKeyBaseName(JMeterLogEntry entry, LineMap lineMap) {
        return lineMap.getNameWithReplacementsFromLine(entry.getUrl());
    }

}
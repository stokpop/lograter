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
package nl.stokpop.lograter.processor.accesslog;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.processor.CounterKeyCreator;
import nl.stokpop.lograter.util.linemapper.LineMap;

import java.util.Collections;
import java.util.List;

public class AccessLogCounterKeyCreator implements CounterKeyCreator<AccessLogEntry> {

    private final List<String> groupByFields;

    public AccessLogCounterKeyCreator() {
        this.groupByFields = Collections.emptyList();
    }

    public AccessLogCounterKeyCreator(final List<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    @Override
    public final CounterKey createCounterKey(final AccessLogEntry entry) {
        return createCounterKey(entry, counterKeyBaseName(entry));
    }

    @Override
    public final CounterKey createCounterKey(final AccessLogEntry entry, final LineMap lineMap) {
        return createCounterKey(entry, counterKeyBaseName(entry, lineMap));
    }

    @Override
    public final CounterKey createCounterKey(final AccessLogEntry entry, final String baseName) {
        return constructCounterKey(entry, baseName, groupByFields);
    }



    public String counterKeyBaseName(AccessLogEntry entry) {
        return entry.getUrl();
    }

    private String counterKeyBaseName(AccessLogEntry entry, LineMap lineMap) {
        return lineMap.getNameWithReplacementsFromLine(entry.getUrl());
    }

}
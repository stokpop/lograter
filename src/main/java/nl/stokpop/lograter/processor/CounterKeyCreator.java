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
package nl.stokpop.lograter.processor;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.CounterKeyMetaData;
import nl.stokpop.lograter.logentry.LogEntry;
import nl.stokpop.lograter.util.linemapper.LineMap;

import java.util.List;
import java.util.stream.Collectors;

public interface CounterKeyCreator<T extends LogEntry> {

    default CounterKey constructCounterKey(LogEntry entry, String baseName, List<String> groupByFields) {
        List<String> fieldValues = groupByFields.stream().map(entry::getField).collect(Collectors.toList());
        CounterKeyMetaData metaData = new CounterKeyMetaData(groupByFields, fieldValues);
        return CounterKey.createCounterKeyWithFieldsInName(baseName, metaData);
    }

    CounterKey createCounterKey(T logEntry);

    CounterKey createCounterKey(T logEntry, LineMap name);

    CounterKey createCounterKey(T logEntry, String baseName);
}

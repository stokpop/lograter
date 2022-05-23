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
package nl.stokpop.lograter.processor;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.logentry.LogEntry;
import nl.stokpop.lograter.util.linemapper.LineMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface CounterKeyCreator<T extends LogEntry> {
    char SEP_CHAR = ',';

    default CounterKey constructCounterKey(LogEntry entry, String baseKey, List<String> groupByFields) {
        StringBuilder key = new StringBuilder(baseKey);
        for (String groupByField : groupByFields) {
            String field = entry.getField(groupByField);
            String sanitizedField = field.replace(",", "_");
            key.append(SEP_CHAR).append(sanitizedField);
        }
        Map<String, String> fields = groupByFields.stream().collect(Collectors.toMap(Function.identity(), entry::getField));
        return CounterKey.of(key.toString(), fields);
    }

    CounterKey createCounterKey(T logEntry);

    CounterKey createCounterKey(T logEntry, LineMap name);

    CounterKey createCounterKey(T logEntry, String baseName);
}

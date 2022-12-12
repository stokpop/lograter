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
package nl.stokpop.lograter.counter;

import com.healthmarketscience.jackcess.util.OleBlob;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Collectors;

public class CounterKey implements Comparable<CounterKey> {

    public static final char SEP_CHAR = ',';
    private final String name;
    private final CounterKeyMetaData metaData;

    private CounterKey(String name, CounterKeyMetaData meta) {
        this.name = name;
        this.metaData = meta;
    }

    /**
     * Adds comma separated list of fields to the name, in the order of the values of the fields in the Map.
     */
    @NotNull
    public static CounterKey createCounterKeyWithFieldsInName(String baseName, CounterKeyMetaData metaData) {
        StringBuilder builder = new StringBuilder(baseName);
        for (String fieldValue : metaData.getValues()) {
            String sanitizedField = fieldValue.replace(",", "_");
            builder.append(SEP_CHAR).append(sanitizedField);
        }
        String key = builder.toString();
        return of(key, metaData);
    }

    public String getName() {
        return name;
    }

    public CounterKeyMetaData getMetaData() {
        return metaData;
    }

    @Override
    public int compareTo(CounterKey other) {
        return name.compareTo(other.name);
    }

    public static CounterKey merge(CounterKey one, CounterKey two) {
        CounterKeyMetaData merged = CounterKeyMetaData.merge(one.getMetaData(), two.getMetaData());
        return new CounterKey(String.join("-", one.getName(), two.getName(), "merged"), merged);
    }

    public static CounterKey of(String name) {
        return new CounterKey(name, CounterKeyMetaData.EMPTY_META_DATA);
    }

    public static CounterKey of(String name, CounterKeyMetaData metaData) {
        return new CounterKey(name, metaData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CounterKey that = (CounterKey) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "CounterKey{" +
                "name='" + name + '\'' +
                ", metaData=" + metaData +
                '}';
    }

    public String toHumanFriendlyString() {
        String keyValueString = metaData.toKeyValueString();
        if (keyValueString.isEmpty()) {
            return name;
        }
        else {
            return String.join("-", name, keyValueString);
        }
    }
}

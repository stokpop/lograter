package nl.stokpop.lograter.counter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CounterKey implements Comparable<CounterKey> {

    private final String name;
    private final Map<String, String> fields;

    private CounterKey(String name, Map<String, String> fields) {
        this.name = name;
        this.fields = Collections.unmodifiableMap(new HashMap<>(fields));
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    @Override
    public int compareTo(CounterKey other) {
        return name.compareTo(other.name);
    }

    public static CounterKey merge(CounterKey one, CounterKey two) {
        Map<String, String> mergeMap = new HashMap<>(one.fields);
        two.fields.forEach((k,v) -> mergeMap.merge(k, v, (value1,value2) -> (! value1.equals(value2)) ? value1 + "+" + value2 : value1));
        return new CounterKey(String.join("-", one.getName(), two.getName(), "merged"), mergeMap);
    }

    public static CounterKey of(String name) {
        return new CounterKey(name, Collections.emptyMap());
    }

    public static CounterKey of(String name, Map<String, String> fields) {
        return new CounterKey(name, fields);
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
}

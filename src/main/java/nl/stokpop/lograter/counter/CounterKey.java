package nl.stokpop.lograter.counter;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Value
public class CounterKey implements Comparable<CounterKey> {
    String name;
    @EqualsAndHashCode.Exclude
    Map<String, String> fields;

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

}

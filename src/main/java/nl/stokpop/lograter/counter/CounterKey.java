package nl.stokpop.lograter.counter;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
}

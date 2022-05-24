package nl.stokpop.lograter.counter;

import net.jcip.annotations.Immutable;
import nl.stokpop.lograter.LogRaterException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps a list of ordered field names and ordered list of values.
 */
@Immutable
public class CounterKeyMetaData {

    public static final CounterKeyMetaData EMPTY_META_DATA = new CounterKeyMetaData(Collections.emptyList(), Collections.emptyList());
    private final List<String> fields;
    private final List<String> values;

    public CounterKeyMetaData(List<String> fields, List<String> values) {
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
        if (fields.size() != values.size()) {
            throw new LogRaterException("Number of fields (" + fields.size() + ") need to be equal to number of values (" + values.size() + ")");
        }
    }

    public List<String> getFields() {
        return fields;
    }

    public List<String> getValues() {
        return values;
    }

    /**
     * @param field to retrieve the value of
     * @return null when field does not exist
     */
    public String get(String field) {
        if (!fields.contains(field)) {
            return null;
        }
        return values.get(fields.indexOf(field));
    }

    public void putAll(PutFieldAndValue function) {
        for (int i = 0; i < fields.size(); i++) {
            function.put(fields.get(i), values.get(i));
        }
    }

    /**
     * Merges fields and values in order, first fields of one, then appended by fields of two if
     * there are non-matching fields. Values are merged into one value if same or to "v1+v2" if the
     * values of one field differs.
     */
    public static CounterKeyMetaData merge(CounterKeyMetaData one, CounterKeyMetaData two) {
        List<String> mergedFields = new ArrayList<>();
        List<String> mergedValues = new ArrayList<>();

        List<String> fields1 = one.fields;
        List<String> fields2 = two.fields;

        List<String> values1 = one.values;
        List<String> values2 = two.values;

        for (int i = 0; i < fields1.size(); i++) {
            String field1 = fields1.get(i);
            String value1 = values1.get(i);

            mergedFields.add(field1);
            if (fields2.contains(field1)) {
                int index = fields2.indexOf(field1);
                String value2 = values2.get(index);
                if (value1.equals(value2)) {
                    mergedValues.add(value1);
                }
                else {
                    mergedValues.add(value1 + '+' + value2);
                }
            }
            else {
                mergedFields.add(value1);
            }
        }
        for (int i = 0; i < fields2.size(); i++) {
            String field2 = fields2.get(i);
            String value2 = values2.get(i);

            if (!mergedFields.contains(field2)) {
                mergedFields.add(field2);
                mergedValues.add(value2);
            }
        }
        return new CounterKeyMetaData(mergedFields, mergedValues);
    }
}

package nl.stokpop.lograter.counter;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class CounterKeyTest extends TestCase {

    public void testCompareTo() {
        assertEquals(-1, CounterKey.of("a").compareTo(CounterKey.of("b")));
        assertEquals(0, CounterKey.of("a").compareTo(CounterKey.of("a")));
        assertEquals(1, CounterKey.of("b").compareTo(CounterKey.of("a")));

        Map<String, String> one = new HashMap<>();
        one.put("httpCode", "200");

        Map<String, String> two = new HashMap<>();
        two.put("httpCode", "500");

        // no fields in compare
        assertEquals(0, new CounterKey("a", one).compareTo(new CounterKey("a", two)));
    }

    public void testMerge() {
        Map<String, String> one = new HashMap<>();
        one.put("httpCode", "200");
        one.put("httpMethod", "GET");
        CounterKey k1 = new CounterKey("foo", one);

        Map<String, String> two = new HashMap<>();
        two.put("httpCode", "500");
        two.put("httpMethod", "GET");
        CounterKey k2 = new CounterKey("bar", two);

        CounterKey merged = CounterKey.merge(k1, k2);

        assertEquals("foo-bar-merged", merged.getName());
        assertEquals("200+500", merged.getFields().get("httpCode"));
        assertEquals("GET", merged.getFields().get("httpMethod"));
    }
}
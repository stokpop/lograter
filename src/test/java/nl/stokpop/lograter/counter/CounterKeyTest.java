package nl.stokpop.lograter.counter;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

import static nl.stokpop.lograter.logentry.LogEntry.HTTP_METHOD;
import static nl.stokpop.lograter.logentry.LogEntry.HTTP_STATUS;

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
        assertEquals(0, CounterKey.of("a", one).compareTo(CounterKey.of("a", two)));
    }

    public void testMerge() {
        Map<String, String> one = new HashMap<>();
        one.put(HTTP_STATUS, "200");
        one.put(HTTP_METHOD, "GET");
        CounterKey k1 = CounterKey.of("foo", one);

        Map<String, String> two = new HashMap<>();
        two.put(HTTP_STATUS, "500");
        two.put(HTTP_METHOD, "GET");
        CounterKey k2 = CounterKey.of("bar", two);

        CounterKey merged = CounterKey.merge(k1, k2);

        assertEquals("foo-bar-merged", merged.getName());
        assertEquals("200+500", merged.getFields().get(HTTP_STATUS));
        assertEquals("GET", merged.getFields().get(HTTP_METHOD));
    }
}
package nl.stokpop.lograter.store;

import org.junit.Test;

import static nl.stokpop.lograter.store.RequestCounterStoreHashMap.OVERFLOW_COUNTER;
import static org.junit.Assert.assertEquals;

public class RequestCounterStoreHashMapTest {

    @Test
    public void add() {
        RequestCounterStore store = new RequestCounterStoreHashMap("testStore", "totalRequests", 1);

        store.add("key1", 1, 1);
        store.add("key2", 2, 2);
        store.add("key3", 2, 2);
        store.add("key4", 2, 2);
        store.add("key1", 2, 2);

        assertEquals("key1 and overflow counter expected", 2, store.getCounterKeys().size());
        assertEquals("two hits on key1 expected", 2, store.get("key1").getHits());
        assertEquals("overflow should have 3 hits", 3, store.get(OVERFLOW_COUNTER).getHits());
    }
}
package nl.stokpop.lograter.store;

import org.junit.Test;

import static nl.stokpop.lograter.store.RequestCounterStoreMaxCounters.OVERFLOW_COUNTER;
import static org.junit.Assert.assertEquals;

public class RequestCounterStoreHashMapTest {

    @Test
    public void addWithMaxUniqueCounters() {
        RequestCounterStore store = new RequestCounterStoreHashMap("testStore", "totalRequests");
        RequestCounterStore storeMax = new RequestCounterStoreMaxCounters(store, 1);

        storeMax.add("key1", 1, 1);
        storeMax.add("key2", 2, 2);
        storeMax.add("key3", 2, 2);
        storeMax.add("key4", 2, 2);
        storeMax.add("key1", 2, 2);

        assertEquals("key1 and overflow counter expected", 2, storeMax.getCounterKeys().size());
        assertEquals("two hits on key1 expected", 2, storeMax.get("key1").getHits());
        assertEquals("overflow should have 3 hits", 3, storeMax.get(OVERFLOW_COUNTER).getHits());
    }
}
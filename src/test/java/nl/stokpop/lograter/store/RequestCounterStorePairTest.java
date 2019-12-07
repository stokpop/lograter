package nl.stokpop.lograter.store;

import nl.stokpop.lograter.counter.CounterStorageType;
import org.junit.Test;

import static nl.stokpop.lograter.store.RequestCounterStoreHashMap.OVERFLOW_COUNTER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RequestCounterStorePairTest {

    @Test
    public void addAndOverflow() {

        RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Memory);
        RequestCounterStore storeSuccess = factory.newInstance("success", "successStore", 1);
        RequestCounterStore storeFailure = factory.newInstance("failure", "failureStore", 1);

        RequestCounterStorePair pair = new RequestCounterStorePair(storeSuccess, storeFailure);

        pair.addSuccess("key1", 0, 10);
        pair.addSuccess("key2", 1, 11);
        pair.addSuccess("key3", 2, 12);
        pair.addFailure("key4", 3, 13);
        pair.addFailure("key1", 4, 14);

        // success and failure should be in balance
        RequestCounterStore successFromPair = pair.getRequestCounterStoreSuccess();
        assertEquals(3, successFromPair.getTotalRequestCounter().getHits());
        assertEquals(2, successFromPair.getCounterKeys().size());
        assertEquals(2, successFromPair.get(OVERFLOW_COUNTER).getHits());
        assertEquals(1, successFromPair.get("key1").getHits());
        assertTrue(successFromPair.isOverflown());

        RequestCounterStore failureFromPair = pair.getRequestCounterStoreFailure();
        assertEquals(2, failureFromPair.getTotalRequestCounter().getHits());
        assertEquals(1, failureFromPair.get(OVERFLOW_COUNTER).getHits());
        assertEquals(1, failureFromPair.get("key1").getHits());
        assertNull("should not contain key4 after overflow", failureFromPair.get("key4"));
        assertTrue(failureFromPair.isOverflown());

    }
}
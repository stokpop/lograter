package nl.stokpop.lograter.store;

import nl.stokpop.lograter.counter.CounterStorageType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static nl.stokpop.lograter.store.RequestCounterStoreMaxCounters.OVERFLOW_COUNTER;
import static org.junit.Assert.assertEquals;

public class RequestCounterStoreExternalSortTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testMax1Unique() {

        final int cap = 1;

        // in order to avoid memory issues, there should be a cap on the number of request counters per counter store
        RequestCounterStore store =  new RequestCounterStoreFactory(CounterStorageType.ExternalSort, temporaryFolder.getRoot())
                .newInstance("mappers-success", "myTestStore", cap);


        store.add("my-counter-" + 1, 0, 0);
        store.add("my-counter-" + 2, 2, 2);
        store.add("my-counter-" + 3, 3, 3);

        // 1 unique keys and one overflow key with 2 entries
        assertEquals(2, store.getCounterKeys().size());
        assertEquals(3, store.getTotalRequestCounter().getHits());
        assertEquals(2, store.get(OVERFLOW_COUNTER).getHits());
    }

}
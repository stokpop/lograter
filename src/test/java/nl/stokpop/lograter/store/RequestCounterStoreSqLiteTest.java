package nl.stokpop.lograter.store;

import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.util.DatabaseBootstrap;
import nl.stokpop.lograter.util.DatabaseBootstrapTest;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static nl.stokpop.lograter.store.RequestCounterStoreMaxCounters.OVERFLOW_COUNTER;
import static org.junit.Assert.assertEquals;

public class RequestCounterStoreSqLiteTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void add() {
        DatabaseBootstrapTest.injectTestDatabasePathIntoSysVars();

        DatabaseBootstrap.instance().bootstrapDatabase(true);

        RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Database, TimePeriod.MAX_TIME_PERIOD);
        RequestCounterStore testStore = factory.newInstance("testStore", "all-requests", 1);

        testStore.add("key1", 1,1);
        testStore.add("key2", 2,2);
        testStore.add("key3", 3,3);
        testStore.add("key4", 4,4);
        testStore.add("key1", 5,5);

        assertEquals("expect key1 and OVERFLOW_COUNTER (and not all-requests)", 2, testStore.getCounterKeys().size());
        assertEquals("expect 2 hits on key1", 2, testStore.get("key1").getHits());
        assertEquals("expect 3 hits on OVERFLOW_COUNTER", 3, testStore.get(OVERFLOW_COUNTER).getHits());
        assertEquals("expect 5 hits in total", 5, testStore.getTotalRequestCounter().getHits());
    }
}
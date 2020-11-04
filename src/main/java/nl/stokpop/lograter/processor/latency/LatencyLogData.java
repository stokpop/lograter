package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.processor.BasicLogData;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;

public class LatencyLogData extends BasicLogData {
	private final RequestCounterStorePair counterStorePair;

	public LatencyLogData(RequestCounterStoreFactory csFactory) {
		RequestCounterStore requestCounterStoreSuccess = csFactory.newInstance("latency-log-counterstore-success");
		RequestCounterStore requestCounterStoreFailure = csFactory.newInstance("latency-log-counterstore-failure");
		this.counterStorePair = new RequestCounterStorePair(requestCounterStoreSuccess, requestCounterStoreFailure);
	}
	
	public RequestCounterStorePair getCounterStorePair() {
		return this.counterStorePair;
	}

    /**
     * Gets reference to total request counter. Only one is expected.
     * If called before one add to the counter store is done, an exception is thrown.
     */
    public RequestCounter getTotalRequestCounter() {
        return counterStorePair.getRequestCounterStoreSuccess().getTotalRequestCounter();
    }

	@Override
	public String toString() {
		return "LatencyLogData{" +
				"counterStorePair=" + counterStorePair +
				"} " + super.toString();
	}

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
}

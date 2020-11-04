package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.clickpath.ClickPathCollector;
import nl.stokpop.lograter.counter.RequestCounterDataBundle;
import nl.stokpop.lograter.store.RequestCounterStorePair;

import java.util.Collections;
import java.util.List;

/**
 * Bundles all latency log data.
 */
public class LatencyLogDataBundle implements RequestCounterDataBundle {
    private final LatencyLogConfig config;
    private final LatencyLogData data;
    private final ClickPathCollector clickPathCollector;

    public LatencyLogDataBundle(LatencyLogConfig config, LatencyLogData data, ClickPathCollector clickPathCollector) {
        this.config = config;
	    this.data = data;
        this.clickPathCollector = clickPathCollector;
    }

    public LatencyLogDataBundle(LatencyLogConfig config, LatencyLogData data) {
       this(config, data, null);
    }

    public ClickPathCollector getClickPathCollector() {
        return clickPathCollector;
    }

	@Override
	public RequestCounterStorePair getTotalRequestCounterStorePair() {
		return data.getCounterStorePair();
	}

	@Override
	public List<RequestCounterStorePair> getRequestCounterStorePairs() {
		return Collections.singletonList(data.getCounterStorePair());
	}

	@Override
	public boolean doesSupportFailureRequestCounters() {
		return false;
	}

	public LatencyLogConfig getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "LatencyLogDataBundle{" +
                "config=" + config +
                ", LatencyLogData=" + data +
                ", clickPathCollector=" + clickPathCollector +
                '}';
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

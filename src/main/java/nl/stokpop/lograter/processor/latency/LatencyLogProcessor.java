package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.logentry.LatencyLogEntry;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.util.ArrayList;
import java.util.List;

public class LatencyLogProcessor implements Processor<LatencyLogEntry> {

	private static final char SEP_CHAR = ',';

	private final LatencyLogData data;
	private final LatencyLogConfig config;
	private final String[] counterFields;
    private final String totalCounterKey;

	public LatencyLogProcessor(LatencyLogData data, LatencyLogConfig config) {
		this.data = data;
		this.config = config;
		String counterFields = config.getCounterFields();
		this.counterFields = counterFieldsToStringArray(counterFields);
        this.totalCounterKey = createTotalCounterKey(this.counterFields);
	}

	public static String[] counterFieldsToStringArray(String counterFields) {
		return counterFields.replace(" ", "").split(",");
	}

	/**
     * For each available counter field, add "TOTAL," to align with columns in text final report.
     */
    private String createTotalCounterKey(String[] counterFields) {
        if (counterFields.length == 0) { return "TOTAL"; }

        List<String> totals = new ArrayList<>();
        for (int i = 0; i < counterFields.length; i++) {
            totals.add("TOTAL");
        }
        return String.join(",", totals);
    }

    @Override
	public void processEntry(LatencyLogEntry entry) {
		
		String counterKey = createCounterKey(entry);

		TimePeriod filterPeriod = config.getFilterPeriod();
		if (!filterPeriod.isWithinTimePeriod(entry.getTimestamp())) {
			data.incFilteredLines();
			return;
		}
		
		long timestamp = entry.getTimestamp();
		
		data.updateLogTime(timestamp);

		int durationInMillis = entry.getDurationInMillis();
		data.getCounterStorePair().addSuccess(counterKey, timestamp, durationInMillis);

	}

    private String createCounterKey(LatencyLogEntry entry) {
		StringBuilder perfCounterName = new StringBuilder();
		for (String field : counterFields) {
			perfCounterName.append(entry.getField(field)).append(SEP_CHAR);
		}
		return perfCounterName.substring(0, perfCounterName.length() - 1);
	}

	public LatencyLogData getData() {
		return data;
	}

	public LatencyLogConfig getConfig() {
		return config;
	}

}

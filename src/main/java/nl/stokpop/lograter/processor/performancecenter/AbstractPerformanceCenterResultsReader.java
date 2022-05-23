/*
 * Copyright (C) 2020 Peter Paul Bakker, Stokpop Software Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.stokpop.lograter.processor.performancecenter;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.CounterKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Common code for multiple physical data sources.
 */
public abstract class AbstractPerformanceCenterResultsReader implements PerformanceCenterResultsReader {

	private static final Logger log = LoggerFactory.getLogger(AbstractPerformanceCenterResultsReader.class);

	private static final int MINUS_ONE = -1;
    private static final int MAX_WARNINGS = 60;

    private AtomicInteger warningThinkTimeCount = new AtomicInteger(0);

	private File resultsDatabaseFile;

	private Integer pcAggregationPeriodSec;

	// use some number so that the rampup lower load is as good as done, hopefully
	protected static final int ROW_FETCH_COUNT_FOR_GRANULARITY_DETERMINATION = 2063;

	public AbstractPerformanceCenterResultsReader(File resultsDatabaseFile) {
		this(resultsDatabaseFile, PerformanceCenterCalculator.determineAggregationPeriod(resultsDatabaseFile));
	}

    /**
	 * Construct the results reader.
	 *
	 * Note that the pcAggregationPeriodSec can be null. In that case an attempt will
	 * be made to determine the aggregation period from the database itself, but that might give an incorrect value.
	 *
	 * @param resultsDatabaseFile the db or mdb results database from performance center
	 * @param pcAggregationPeriodSec the aggregation used in the database (e.g. as specified in the .lra file)
	 */
	public AbstractPerformanceCenterResultsReader(File resultsDatabaseFile, Integer pcAggregationPeriodSec) {
		this.resultsDatabaseFile = resultsDatabaseFile;
		this.pcAggregationPeriodSec = pcAggregationPeriodSec;
	}

	public File getResultsDatabaseFile() {
		return resultsDatabaseFile;
	}

	@Override
	public void addEventsToResultsData(EventMeter eventMeter, PerformanceCenterResultsData data, Map<Integer, PerformanceCenterEvent> eventMap, long testStartTimeSecEpoch, long granularityMs) {
		if (eventMeter.getEventID() == MINUS_ONE) {
			log.debug("Skipping row with Event ID -1 for [{}].", eventMeter);
			return;
		}

		String eventName = eventMap.get(eventMeter.getEventID()).getEventName();
		if (eventName.startsWith("_")) {
			log.debug("Skip event that starts with underscore (_) [{}].", eventName);
			return;
		}

		CounterKey key = CounterKey.of(eventName);

		long timestampMs = (testStartTimeSecEpoch + (long)eventMeter.getEndTime()) * 1000;
		// wasted time is already substracted from this value by performance center analysis
		// think time (inside the transaction) needs to be substracted
		final double durationSecWithoutThinkTime = durationSecWithoutThinkTime(eventMeter, eventMeter.getValue(), eventName);
		if (durationSecWithoutThinkTime < 0) {
		    throw new LogRaterException("Duration (value) minus think time is below 0 for [" + eventMeter + "]");
        }
		final int durationMsWithoutThinkTime = (int) (durationSecWithoutThinkTime * 1000.0);

		final double valueMinSecWithoutThinkTime = durationSecWithoutThinkTime(eventMeter, eventMeter.getValueMin(), eventName);
		final double valueMaxSecWithoutThinkTime = durationSecWithoutThinkTime(eventMeter, eventMeter.getValueMax(), eventName);

		// spread the hits depending on granularity of the analysis
		final int count = eventMeter.getCount();
		final long timePeriodBetweenEventsMs = granularityMs / count;
		if (count == 1) {
			// assume that value == valueMin == valueMax
			if (eventMeter.isSuccess()) {
				data.addSuccess(key, timestampMs, durationMsWithoutThinkTime);
			}
			else {
				data.addFailure(key, timestampMs, durationMsWithoutThinkTime);
			}
		}
		else if (count == 2) {
			// add valueMin and valueMax
			final int durationMinWithoutThinkTimeMs = (int) (valueMinSecWithoutThinkTime * 1000);
			final int durationMaxWithoutThinkTimeMs = (int) (valueMaxSecWithoutThinkTime * 1000);
			if (eventMeter.isSuccess()) {
				data.addSuccess(key, timestampMs, durationMinWithoutThinkTimeMs);
				data.addSuccess(key, timestampMs + timePeriodBetweenEventsMs, durationMaxWithoutThinkTimeMs);
			} else {
				data.addFailure(key, timestampMs, durationMinWithoutThinkTimeMs);
				data.addFailure(key, timestampMs + timePeriodBetweenEventsMs, durationMaxWithoutThinkTimeMs);
			}
		}
		else if (count > 2) {
			final double average = eventMeter.getValue();
			// add valueMin and valueMax, without think time adjustment!
			List<Double> durationsSec = PerformanceCenterCalculator.createDataSet(count, eventMeter.getValueMin(), eventMeter.getValueMax(), average);

			for (int i = 0; i < durationsSec.size(); i++) {
				final long eventTimeStamp = timestampMs + (i * timePeriodBetweenEventsMs);
                final double localDurationSec = durationsSec.get(i);

                final double localDurationSecWithoutThinktime = durationSecWithoutThinkTime(eventMeter, localDurationSec, eventName);
                final int localDurationMsWithoutThinktime = (int) (localDurationSecWithoutThinktime * 1000);

                if (eventMeter.isSuccess()) {
                    // note: for successes the think time is substracted
                    data.addSuccess(key, eventTimeStamp, localDurationMsWithoutThinktime);
				}
				else {
                	// note: for failures the think time is not substracted
					data.addFailure(key, eventTimeStamp, localDurationMsWithoutThinktime);
				}
			}
		}
		else {
			log.warn("Unexpected count (0 or below 0) for {}.", eventMeter);
		}

	}

    /**
     * @return duration minus thinktime in seconds, or just duration when eventMeter is no succes
     */
    private double durationSecWithoutThinkTime(EventMeter eventMeter, double durationSec, String eventName) {
        double thinkTimeSec = eventMeter.getThinkTime();
        double durationSecWithoutThinktime = durationSec - thinkTimeSec;
        double durationSecForSuccessOrFailure = eventMeter.isSuccess() ? durationSecWithoutThinktime : durationSec;
        if (durationSecForSuccessOrFailure < 0) {
            String message = "Duration ({}) minus think time ({}) is below 0 for [{}]: will return 0 seconds now! [{}]";
            if (warningThinkTimeCount.incrementAndGet() < MAX_WARNINGS) {
                log.warn(message, durationSec, thinkTimeSec, eventName, eventMeter);
            } else {
                log.debug(message, durationSec, thinkTimeSec, eventName, eventMeter);
            }
            if (warningThinkTimeCount.intValue() % 1000 == 0) {
                log.warn("Total warnings about 'duration minus think time below 0': [{}]", warningThinkTimeCount.intValue());
            }
            return 0;
        }
        else {
			return durationSecForSuccessOrFailure;
		}
    }

    protected Integer getPcAggregationPeriodSec() {
		return pcAggregationPeriodSec;
	}

	protected boolean isAggregationPeriodValid(Integer pcAggregationPeriodSec) {
		// 0 mean no aggregation?
        return pcAggregationPeriodSec != null && pcAggregationPeriodSec >= 0;
    }
}

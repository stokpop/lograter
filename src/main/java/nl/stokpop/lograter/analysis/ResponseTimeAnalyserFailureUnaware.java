/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.analysis;

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.store.TimeMeasurement;
import nl.stokpop.lograter.util.ConcurrentSoftCache;
import nl.stokpop.lograter.util.metric.MetricPoint;
import nl.stokpop.lograter.util.metric.MetricsWindow;
import nl.stokpop.lograter.util.time.TPSMeasurement;
import nl.stokpop.lograter.util.time.TimePeriod;
import nl.stokpop.lograter.util.time.TimeWindowCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Analyser for a request counter to determine all kinds of statistics.
 * 
 * Does not consider failures and successes separately. Use FailureAware analysers when needed.
 * 
 * Make sure not to hand over the same counter to multiple analysers
 * that can be analysed concurrently.
 */
@NotThreadSafe
public class ResponseTimeAnalyserFailureUnaware implements ResponseTimeAnalyser {

	private static final Logger log = LoggerFactory.getLogger(ResponseTimeAnalyserFailureUnaware.class);

    public static final int GRAPH_HISTO_NUMBER_OF_RANGES = 100;
    public static final int HIGHEST_PERCENTILE = 100;

    private final TimeWindowCalculator timeWindowCalculator;

	private final RequestCounter requestCounter;

	private final ConcurrentSoftCache<String, TransactionCounterResult> tcrCache;

	/**
	 * Analyse the request counter for the whole available time period.
	 */
	public ResponseTimeAnalyserFailureUnaware(RequestCounter counter) {
		this(counter, counter.getTimePeriod());
	}

	/**
	 * Analyse the request counter for the specified time period. If the counter contains more
	 * data outside of the given time period, then a sliced counter is created internally for
	 * the given time period.
	 */
    public ResponseTimeAnalyserFailureUnaware(RequestCounter counter, TimePeriod timePeriod) {
    	this.tcrCache = new ConcurrentSoftCache<>();
    	RequestCounter slicedCounter = RequestCounter.safeSlicedCounter(counter, timePeriod);
	    this.requestCounter = slicedCounter;
	    this.timeWindowCalculator = new TimeWindowCalculator(requestCounter.getUniqueCounterKey().getName(), slicedCounter, timePeriod);
    }

	@Override
    public CounterKey getCounterKey() {
		return requestCounter.getCounterKey();
	}
	
	@Override
    public RequestCounter getCounter() {
		return this.requestCounter;
	}
	
	@Override
    public long percentileHitDuration(double percentile) {
		return determinePercentile(percentile);
	}

	private long determinePercentile(double percentile) {
        return timeWindowCalculator.determinePercentile(percentile);
	}

    @Override
    public TransactionCounterResult maxHitsPerSecond() {
		return maxHitsPerDuration(1000);
	}

	@Override
    public TransactionCounterResult maxHitsPerMinute() {
		return maxHitsPerDuration(1000L * 60);
	}

	@Override
    public TransactionCounterResult maxHitsPerMinuteWithTpsMeasurements() {
		return maxHitsPerDuration(1000L * 60, true);
	}

	@Override
    public TransactionCounterResult maxHitsPerSecondWithTpsMeasurements() {
		return maxHitsPerDuration(1000, true);
	}

	@Override
    public TransactionCounterResult maxHitsPerHour() {
		return maxHitsPerDuration(1000L * 60 * 60);
	}

	@Override
    @SuppressWarnings("boxing")
	public TransactionCounterResult maxHitsPerDuration(long durationInMillis) {
		return maxHitsPerDuration(durationInMillis, false);
	}

	@Override
    @SuppressWarnings("boxing")
	public TransactionCounterResult maxHitsPerDuration(
            final long timeBucketPeriod,
            final boolean includeTpsMeasurements) {

        // prevent same calculations multiple times
		final String cacheKey = timeBucketPeriod + Boolean.toString(includeTpsMeasurements);
		final TransactionCounterResult cachedTcr = tcrCache.get(cacheKey);
		if (cachedTcr != null) {
			log.debug("Cache hit for [{}] for cacheKey [{}]: return [{}]", this.getCounter().getUniqueCounterKey(), cacheKey, cachedTcr);
			return cachedTcr;
		}

		final Queue<Long> hitsTimestampQueue = new ArrayDeque<>();

		long maxPerDurationUntilNow = 0;
		long maxPerDurationUntilNowTimestamp = 0;

		for (TimeMeasurement timeMeasurement : requestCounter) {
			final Long currentTimestamp = timeMeasurement.getTimestamp();

			hitsTimestampQueue.add(currentTimestamp);

            long beginningOfTimeBucket = currentTimestamp - timeBucketPeriod;

            if (!hitsTimestampQueue.isEmpty()) {
	            // <= to exclude the same second/millisecond in last duration (was <)
	            while (hitsTimestampQueue.peek() <= beginningOfTimeBucket) {
		            hitsTimestampQueue.remove();
	            }

	            // keep track of highest number if hits in a time period
	            if (hitsTimestampQueue.size() > maxPerDurationUntilNow) {
		            maxPerDurationUntilNow = hitsTimestampQueue.size();
		            maxPerDurationUntilNowTimestamp = hitsTimestampQueue.peek();
	            }
            }

		}

        List<TPSMeasurement> tpsPerTimestamp = includeTpsMeasurements
                ? avgTpsPerTimeBucket(timeBucketPeriod, maxPerDurationUntilNowTimestamp)
                : new ArrayList<>();

		// If total period duration is smaller than the duration to calculate max value for,
		// the timestamp could be less than 0, set to 0 in that case.
		long maxHitsPerDurationTimestamp = maxPerDurationUntilNowTimestamp < 0
                ? 0
                : maxPerDurationUntilNowTimestamp;

        final TransactionCounterResult result =
                new TransactionCounterResult(tpsPerTimestamp, maxPerDurationUntilNow, timeBucketPeriod, maxHitsPerDurationTimestamp);

		tcrCache.put(cacheKey, result);

		return result;
	}

	private List<TPSMeasurement> avgTpsPerTimeBucket(final long timeBucketPeriod, final long maxHitsTimestamp) {
		final List<TPSMeasurement> tpsMeasurements = new ArrayList<>();
		final Queue<Long> totalTimestampQueue = new ArrayDeque<>();
		requestCounter.forEach(tm -> totalTimestampQueue.add(tm.getTimestamp()));

		// determine bucket offset to use to retain the highest avg TPS value in a minute
		// also make sure all tps measurements are within the original time period of counter
		long startTime = requestCounter.getTimePeriod().getStartTime();
		long endTime = requestCounter.getTimePeriod().getEndTime();

		long moduloMaxHitsTimestamp = maxHitsTimestamp % timeBucketPeriod;
		long moduloStartTimestamp = startTime % timeBucketPeriod;

		final long firstBucketStartTime = startTime + (moduloMaxHitsTimestamp - moduloStartTimestamp) - timeBucketPeriod;

		final int extraBuckets = firstBucketStartTime < startTime ? 2 : 1;
		final long lastBucketEndTime = (long) ((Math.ceil(endTime / (double) timeBucketPeriod) + extraBuckets) * timeBucketPeriod);

		final long halfTimeBucket = timeBucketPeriod / 2;

		long currentBucketStartTime = firstBucketStartTime;
		long currentBucketEndTime = firstBucketStartTime + timeBucketPeriod;

		while (currentBucketEndTime < lastBucketEndTime) {
			int countHits = 0;

			while (!totalTimestampQueue.isEmpty() && totalTimestampQueue.peek() < currentBucketEndTime) {
				totalTimestampQueue.remove();
				countHits = countHits + 1;
			}

			// float reduces memory usage
			float avgHitsPerDuration = (float) countHits / TimeUnit.MILLISECONDS.toSeconds(timeBucketPeriod);
			long tpsMeasurementTimestamp = currentBucketStartTime + halfTimeBucket;

			if (tpsMeasurementTimestamp > startTime && tpsMeasurementTimestamp < endTime) {
				TPSMeasurement tpsMeasurement = new TPSMeasurement(tpsMeasurementTimestamp, avgHitsPerDuration);
				tpsMeasurements.add(tpsMeasurement);
			}

			currentBucketStartTime = currentBucketStartTime + timeBucketPeriod;
			currentBucketEndTime = currentBucketEndTime + timeBucketPeriod;
		}
		return tpsMeasurements;
	}

	@Override
    public ConcurrentCounterResult maxConcurrentRequests() {

		PriorityQueue<Long> concurrentRequests = new PriorityQueue<>();
		
		long maxConcurrentRequests= 0;
		long maxConcurrentRequestsTimestamp = 0;

		for (TimeMeasurement timeMeasurement : requestCounter) {
			long timestamp = timeMeasurement.getTimestamp();
			long endOfRequest = timestamp + timeMeasurement.getDurationInMillis();
			concurrentRequests.add(endOfRequest);
			
			while (!concurrentRequests.isEmpty() && concurrentRequests.peek() <= timestamp) {
				concurrentRequests.remove();
			}
			
			if (concurrentRequests.size() > maxConcurrentRequests) {
				maxConcurrentRequests = concurrentRequests.size();
				// register start of the busy period
				maxConcurrentRequestsTimestamp = concurrentRequests.element();
			}
		}
		
		return new ConcurrentCounterResult(maxConcurrentRequests, maxConcurrentRequestsTimestamp);
	}

    @Override
    public double avgTps() {
        final double totalDurationInSeconds = timeWindowCalculator.getTimeWindowPeriod().getDurationInSeconds();
        return totalDurationInSeconds < 1 ? totalHits() : totalHits() / totalDurationInSeconds;
    }

    @Override
    public double durationInHours() {
        return (timeWindowCalculator.getTimeWindowPeriod().getDurationInMillis()) / (60d * 60d * 1000d);
    }

	/**
	 * The histogram with relevant values is a histogram with data from the 1st percentile to the 99th percentile.
	 * This to avoid very low and very high values to stretch the histogram buckets to timeout values.
	 */
    @Override
    public HistogramData histogramForRelevantValues(int graphHistoNumberOfRanges) {
        return timeWindowCalculator.getHistogram(graphHistoNumberOfRanges, 0, 99);
    }

    @Override
    public List<MetricPoint> metricPoints() {
        // create 200 points for a graph, with at least 1000 ms per point/window
        long pointDistance = Math.max(1000, timeWindowCalculator.getTimeWindowPeriod().getDurationInMillis() / 200);

        final List<MetricPoint> metricPoints = new ArrayList<>();
        String name = requestCounter.getUniqueCounterKey().getName();

        // keep window size equal to pointDistance so no overlap in windows exists
        MetricsWindow metricsWindow = new MetricsWindow(name, pointDistance);
        metricsWindow.processDataSet(requestCounter, metricPoints::add);
        return metricPoints;
    }
    
    @Override
    public double percentage(long overallTotalHits) {
        return (totalHits() / (overallTotalHits + Double.MIN_VALUE)) * 100d;
    }

	@Override
    public double stdDevHitDuration() {
		return timeWindowCalculator.getStdDevDuration();
	}

    @Override
    public long hitsInMinuteWithStartTime(long startTimeStamp) {

	    TimePeriod oneMinute = TimePeriod.createExcludingEndTime(startTimeStamp, startTimeStamp + (1000 * 60));

		RequestCounter oneMinuteRequestCounter = requestCounter.getTimeSlicedCounter(oneMinute);

	    return oneMinuteRequestCounter.getHits();
	}

	@Override
    public long totalHits() {
		return timeWindowCalculator.getSize();
	}

	@Override
    public double avgHitDuration() {
		return timeWindowCalculator.getAverageDuration();

	}

	@Override
    public long min() {
    	return timeWindowCalculator.getMinDuration();
	}

	@Override
    public long max() {
        return timeWindowCalculator.getMaxDuration();
	}

	@Override
    public long percentilePlus(double percentile) {
        return timeWindowCalculator.determinePercentile(percentile);
    }

    @Override
    public long[] percentiles() {
        return percentiles(HIGHEST_PERCENTILE);
	}

	@Override
    public long[] percentiles(int highest) {
    	if (highest > HIGHEST_PERCENTILE) highest = HIGHEST_PERCENTILE;
    	if (highest < 1) throw new LogRaterException("Cannot have highest percentile below 1 [" + highest + "]");

		long[] percentiles = new long[highest];

		for (int percentile = 0; percentile < highest; percentile++) {
			percentiles[percentile] = timeWindowCalculator.determinePercentile(percentile + 1);
		}

		return percentiles;
	}

    @Override
    public TimePeriod getAnalysisTimePeriod() {
        return timeWindowCalculator.getTimeWindowPeriod();
    }

    @Override
    public boolean hasAnyHits() {
        return totalHits() > 0;
    }

    @Override
	public String toString() {
		return "ResponseTimeAnalyser{" + "timeWindowCalculator=" + timeWindowCalculator + ", requestCounter=" + requestCounter + '}';
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException();
	}


}

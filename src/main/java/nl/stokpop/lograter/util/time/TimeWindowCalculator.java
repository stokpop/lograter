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
package nl.stokpop.lograter.util.time;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.analysis.HistogramData;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.store.TimeMeasurement;
import nl.stokpop.lograter.util.Calculator;
import nl.stokpop.lograter.util.metric.MetricPoint;
import org.HdrHistogram.Histogram;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TimeWindowCalculator {

	private Logger log = LoggerFactory.getLogger(TimeWindowCalculator.class);
    private final String name;

    private final TimePeriod timeWindowPeriod;
    private final long size;

    private final double averageDuration;
    private final double averageHitsPerSec;
	private final double stdDevDuration;

	private final int minDuration;
	private final int maxDuration;

	private final Histogram histogram;

    /**
     * @see TimeWindowCalculator
     */
    public TimeWindowCalculator(String name, RequestCounter requestCounter) {
        this(name, requestCounter, requestCounter.getTimePeriod());
    }

    /**
     * This calculator will use the request counter data for calculations.
     * The timeMeasurements should be ordered on timestamp.
     */
    public TimeWindowCalculator(String name, RequestCounter requestCounter, TimePeriod windowTimePeriod) {
	    if (!windowTimePeriod.hasBothTimestampsSet()) { throw new LogRaterException("Do not pass in an unset time period for " + requestCounter.getUniqueCounterKey());}
        this.name = name;
        this.size = requestCounter.getHits();
        this.timeWindowPeriod = windowTimePeriod;
        this.averageHitsPerSec = (double) this.size / timeWindowPeriod.getDurationInSeconds();

	    SummaryStatistics summaryStatistics = new SummaryStatistics();

	    for (TimeMeasurement timeMeasurement : requestCounter) {
		    int duration = timeMeasurement.getDurationInMillis();
		    summaryStatistics.addValue(duration);
	    }
	    this.minDuration = (int) summaryStatistics.getMin();
	    this.maxDuration = (int) summaryStatistics.getMax();
	    double mean = summaryStatistics.getMean();
	    this.averageDuration = Double.isNaN(mean) ? 0.0d : mean;
	    double standardDeviation = summaryStatistics.getStandardDeviation();
	    this.stdDevDuration = Double.isNaN(standardDeviation) ? 0.0d : standardDeviation;
	    
	    int highestTrackableValue = Math.max(maxDuration, 2);
	    this.histogram = new Histogram(1, highestTrackableValue, 5);
	    for (TimeMeasurement timeMeasurement : requestCounter) {
		    int duration = timeMeasurement.getDurationInMillis();
		    this.histogram.recordValue(duration);
		    summaryStatistics.addValue(duration);
	    }
    }

    public long determinePercentile(double percentile) {
        if (percentile < 0 || percentile > 100) {
            throw new LogRaterException("Percentile should be between 0 and 100, now: " + percentile);
        }
        return histogram.getValueAtPercentile(percentile);
    }

    public long determinePercentile(int percentile) {
        if (percentile < 1 || percentile > 100) {
            throw new LogRaterException("Percentile is between (including) 1 and 100, now: " + percentile);
        }

	    return histogram.getValueAtPercentile(percentile);
    }

	public double getStdDevDuration() {
		return stdDevDuration;
	}

    public long getMinDuration() {
        return minDuration;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public double getAverageDuration() {
        return averageDuration;
    }

    public double getAverageHitsPerSec() {
        return averageHitsPerSec;
    }

    public MetricPoint createMetricPoint(long nextPointTimestamp) {
        double hitsPerSec = getAverageHitsPerSec();
        double percentile99 = determinePercentile(99);
        double percentile95 = determinePercentile(95);
        double firstQuartile = determinePercentile(25);
        double thirdQuartile = determinePercentile(75);
        double median = determinePercentile(50);
        double min = getMinDuration();
        double max = getMaxDuration();
        double avg = getAverageDuration();
        return new MetricPoint(nextPointTimestamp, timeWindowPeriod, avg, median, firstQuartile, thirdQuartile, min, max, percentile99, percentile95, hitsPerSec);
    }

    public long getSize() {
        return size;
    }

    public TimePeriod getTimeWindowPeriod() {
        return timeWindowPeriod;
    }

	/**
     * Determine the histogram: number of occurrences in a certain response time range.
     * @param nrOfRanges of ranges to use
     * @param minPercentile the minimum percentile to use for this histogram
     * @param maxPercentile the maximum percentile to use for this histogram
     */
    public HistogramData getHistogram(int nrOfRanges, int minPercentile, int maxPercentile) {

	    if (nrOfRanges <= 0) {
		    throw new LogRaterException("At least 1 range should be given for a histogram.");
	    }

	    if (minPercentile < 0 || minPercentile > 100) {
		    throw new LogRaterException(String.format("Min percentile should be above 0 and below 99: %d", minPercentile));
	    }

	    if (maxPercentile < 0 || maxPercentile > 100) {
		    throw new LogRaterException(String.format("Min percentile should be above 0 and below 99: %d", maxPercentile));
	    }

	    if (maxPercentile - minPercentile < 0) {
		    throw new LogRaterException(String.format("Min percentile (%d) should be lower than max percentile (%d).", minPercentile, maxPercentile));
	    }

	    final Map<Long, Long> histogramMap = new HashMap<>();

        final long localMinDuration = this.histogram.getValueAtPercentile(minPercentile);
        final long localMaxDuration = this.histogram.getValueAtPercentile(maxPercentile);

        long timeRangeInMs = (localMaxDuration - localMinDuration) / nrOfRanges;
        if (timeRangeInMs <= 0) timeRangeInMs = 1;

        timeRangeInMs = Calculator.closestRoundedNumberOfLog10(timeRangeInMs);

        log.debug("histogram of {} with size {} for min: {} max: {} with {} ranges of {} ms",
	        name, size, localMinDuration, localMaxDuration, nrOfRanges, timeRangeInMs);

	    long startRange = ((localMinDuration / timeRangeInMs) * timeRangeInMs);
        long endRange = startRange + timeRangeInMs;

	    while (startRange <= localMaxDuration) {
		    long counter = this.histogram.getCountBetweenValues(startRange, (endRange - 1));
		    histogramMap.put(endRange, counter);
		    startRange = startRange + timeRangeInMs;
		    endRange = endRange + timeRangeInMs;
	    }

        long checkTotal = 0;
        for (Long count : histogramMap.values()) {
            checkTotal = checkTotal + count;
        }

	    final long totalCount = this.histogram.getTotalCount();

	    log.debug("histogram for {} size: {} counted: {}", name, totalCount, checkTotal);

	    if (totalCount != checkTotal) {
		    log.debug("Total count of the histogram {} is not equal to the count of separate values {} for {}.", totalCount, checkTotal, this);
	    }

	    return new HistogramData(histogramMap, timeRangeInMs, localMinDuration, localMaxDuration);
    }

    @Override
	public String toString() {
		return "TimeWindowCalculator{" + "name='" + name + '\''
				+ ", timeWindowPeriod=" + timeWindowPeriod
				+ ", size=" + size
				+ ", averageDuration=" + averageDuration
				+ ", averageHitsPerSec=" + averageHitsPerSec
				+ ", stdDevDuration=" + stdDevDuration
				+ ", minDuration=" + minDuration
				+ ", maxDuration=" + maxDuration + '}';
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

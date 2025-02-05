/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.util.metric;

import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.util.time.TimePeriod;
import nl.stokpop.lograter.util.time.TimeWindowCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A time window of measurements that outputs min, max, average and percentiles over that window for a defined period of time.
 */
public class MetricsWindow {

    private static final Logger log = LoggerFactory.getLogger(MetricsWindow.class);

    private final String name;
    private final long windowSizeInMillis;
    private final long windowSizeInMillisHalf;

    public MetricsWindow(String name, long windowSizeInMillis) {
        this.name = name;
        this.windowSizeInMillis = windowSizeInMillis;
        this.windowSizeInMillisHalf = windowSizeInMillis / 2;
    }

    /**
     * Process a request counter data set.
     * @param requestCounter data should be ordered on timestamp
     * @param callback supply an implementation of the MetricsWindowCallback interface for further processing
     */
    public void processDataSet(RequestCounter requestCounter, MetricsWindowCallback callback) {

        log.debug("Processing data set in MetricsWindow [{}] with window size [{}] ms, one data point every [{}] ms for time measurement collection with size [{}]",
                name, windowSizeInMillis, windowSizeInMillis, requestCounter.getHits());

        long startTime = requestCounter.getTimePeriod().getStartTime();
        // note: endtime is exclusive
        long endTime = requestCounter.getTimePeriod().getEndTime();

        long firstPointTimestamp = startTime + windowSizeInMillisHalf;
        long lastPointTimestamp = endTime - windowSizeInMillisHalf;
        for (long nextPointTimestamp = firstPointTimestamp; nextPointTimestamp <= lastPointTimestamp; nextPointTimestamp += windowSizeInMillis) {
            long startWindowTimestamp = Math.max(startTime, nextPointTimestamp - windowSizeInMillisHalf);
            long endWindowTimestamp = Math.min(endTime, nextPointTimestamp + windowSizeInMillisHalf);

            TimePeriod timeWindow = TimePeriod.createExcludingEndTime(startWindowTimestamp, endWindowTimestamp);
	        RequestCounter subRequestCounter = requestCounter.getTimeSlicedCounter(timeWindow);
            TimeWindowCalculator calculator = new TimeWindowCalculator("Calculator for " + name, subRequestCounter, timeWindow);

            MetricPoint point = calculator.createMetricPoint(nextPointTimestamp);
            callback.addMetricPoint(point);
        }
    }
	
}

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
package nl.stokpop.lograter.report;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.store.TimeMeasurementStoreInMemory;
import nl.stokpop.lograter.util.metric.MetricPoint;
import nl.stokpop.lograter.util.metric.MetricsWindow;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MetricsWindowTest {

	private static final double LITTLE_DELTA = 0.0001;

	@Test
    public void testProcessLargeDataSet()  {

        final MetricsWindow window = new MetricsWindow("Test large window", 1000);

        final RequestCounter requestCounter = createTimeMeasurementsTestSet(1000, true);

        final List<MetricPoint> points = new ArrayList<>();

        window.processDataSet(requestCounter, points::add);

        assertEquals("Expected 1 metric point", 1, points.size());

        for (MetricPoint point : points) {
            assertEquals(point.toString(), 500.5, point.getAverageResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 1000, point.getHitsPerSecond(), LITTLE_DELTA);
            assertEquals(point.toString(), 250, point.getFirstQuartileResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 1000, point.getMaxResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 1, point.getMinResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 500, point.getMedianResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 750, point.getThirdQuartileResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 950, point.getPercentile95ResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 990, point.getPercentile99ResponseTimeMillis(), LITTLE_DELTA);
        }
    }

    @Test
    public void testProcessLargeDataSetMultiplePointsNoOverlap() {

        final MetricsWindow window = new MetricsWindow("Test large window", 100);

        final RequestCounter requestCounter = createTimeMeasurementsTestSet(1000, false);

        final List<MetricPoint> points = new ArrayList<>();

        window.processDataSet(requestCounter, points::add);

        assertEquals("Expected 10 metric points", 10, points.size());
        int i = 1;
        for (MetricPoint point : points) {
            String message = "Point " + i + " " + point.toString();
            System.out.println(message);
            // TODO: why the one off?
            assertEquals(message, 1000, point.getHitsPerSecond(), 12.0);
            i = i + 1;
        }
    }

    @Test
    public void testProcessSmallDataSet() {

        final MetricsWindow window = new MetricsWindow("Test small window", 10);

        final RequestCounter requestCounter = createTimeMeasurementsTestSet(10, false);

        final List<MetricPoint> points = new ArrayList<>();

        window.processDataSet(requestCounter, points::add);

        assertEquals("Expected 1 metric points", 1, points.size());
        for (MetricPoint point : points) {
            assertEquals(point.toString(), 5.5, point.getAverageResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 1000, point.getHitsPerSecond(), LITTLE_DELTA);
            assertEquals(point.toString(), 3, point.getFirstQuartileResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 10, point.getMaxResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 1, point.getMinResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 5, point.getMedianResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 8, point.getThirdQuartileResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 10, point.getPercentile95ResponseTimeMillis(), LITTLE_DELTA);
            assertEquals(point.toString(), 10, point.getPercentile99ResponseTimeMillis(), LITTLE_DELTA);
        }
    }

    @Test
    public void testHalfFilledWindow() {

        final MetricsWindow window = new MetricsWindow("Test half filled small window", 100);

	    final RequestCounter requestCounter = createTimeMeasurementsHalfTestSet(100);

	    final List<MetricPoint> points = new ArrayList<>();

        window.processDataSet(requestCounter, points::add);

        assertEquals("Expected 0 metric points", 0, points.size());
    }

    private RequestCounter createTimeMeasurementsTestSet(final int size, final boolean shuffle) {
        final RequestCounter requestCounter = new RequestCounter(CounterKey.of("Test"), new TimeMeasurementStoreInMemory());

	    final List<Integer> durations = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
        	// i + 1: all calculations work better with 1 ms to X ms instead of 0 ms to X ms
            durations.add(i + 1);
        }
        if (shuffle) {
            Collections.shuffle(durations);
        }
        for (int i = 0; i < size; i++) {
            long offset = new DateTime(1981, 11, 29, 10, 0).getMillis();
            requestCounter.incRequests(offset + i, durations.get(i));
        }
        return requestCounter;
    }

    private RequestCounter createTimeMeasurementsHalfTestSet(int size) {
        final RequestCounter requestCounter = new RequestCounter(CounterKey.of("Test"), new TimeMeasurementStoreInMemory());
        for (int i = size / 2; i < size; i++) {
            requestCounter.incRequests(i, i);
        }
        return requestCounter;
    }
}
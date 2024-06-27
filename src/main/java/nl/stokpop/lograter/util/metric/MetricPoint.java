/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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

import nl.stokpop.lograter.util.time.TimePeriod;

/**
 * Holds basic info for a point in the MetricWindow.
 */
public class MetricPoint {

    private double averageResponseTimeMillis;
    private double medianResponseTimeMillis;
    private double firstQuartileResponseTimeMillis;
    private double thirdQuartileResponseTimeMillis;
    private double minResponseTimeMillis;
    private double maxResponseTimeMillis;
    private double percentile99ResponseTimeMillis;
    private double percentile95ResponseTimeMillis;
    private double hitsPerSecond;
    private long timestamp;
    private final TimePeriod timeWindow;

    public MetricPoint(long timestamp, TimePeriod timeWindow,  double average, double median, double firstQuartile, double thirdQuartile, double min, double max, double percentile99, double percentile95, double hitsPerSecond) {
        this.timestamp = timestamp;
        this.timeWindow = timeWindow;
        this.averageResponseTimeMillis = average;
        this.medianResponseTimeMillis = median;
        this.firstQuartileResponseTimeMillis = firstQuartile;
        this.thirdQuartileResponseTimeMillis = thirdQuartile;
        this.minResponseTimeMillis = min;
        this.maxResponseTimeMillis = max;
        this.percentile99ResponseTimeMillis = percentile99;
        this.percentile95ResponseTimeMillis = percentile95;
        this.hitsPerSecond = hitsPerSecond;
    }

    public double getAverageResponseTimeMillis() {
        return averageResponseTimeMillis;
    }

    public double getHitsPerSecond() {
        return hitsPerSecond;
    }

    public double getMedianResponseTimeMillis() {
        return medianResponseTimeMillis;
    }

    public double getFirstQuartileResponseTimeMillis() {
        return firstQuartileResponseTimeMillis;
    }

    public double getThirdQuartileResponseTimeMillis() {
        return thirdQuartileResponseTimeMillis;
    }

    public double getMinResponseTimeMillis() {
        return minResponseTimeMillis;
    }

    public double getMaxResponseTimeMillis() {
        return maxResponseTimeMillis;
    }

    public double getPercentile99ResponseTimeMillis() {
        return percentile99ResponseTimeMillis;
    }

    public double getPercentile95ResponseTimeMillis() {
        return percentile95ResponseTimeMillis;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getStartWindowTimestamp() {
        return timeWindow.getStartTime();
    }

    public long getEndWindowTimestamp() {
        return timeWindow.getEndTime();
    }

    @Override
    public String toString() {
        return "MetricPoint{" +
                "averageResponseTimeMillis=" + averageResponseTimeMillis +
                ", medianResponseTimeMillis=" + medianResponseTimeMillis +
                ", firstQuartileResponseTimeMillis=" + firstQuartileResponseTimeMillis +
                ", thirdQuartileResponseTimeMillis=" + thirdQuartileResponseTimeMillis +
                ", minResponseTimeMillis=" + minResponseTimeMillis +
                ", maxResponseTimeMillis=" + maxResponseTimeMillis +
                ", percentile99ResponseTimeMillis=" + percentile99ResponseTimeMillis +
                ", percentile95ResponseTimeMillis=" + percentile95ResponseTimeMillis +
                ", hitsPerSecond=" + hitsPerSecond +
                ", timestamp=" + timestamp +
                ", startWindowTimestamp=" + timeWindow.getHumanReadableStartTimestamp() +
                ", endWindowTimestamp=" + timeWindow.getHumanReadableEndTimestamp() +
                '}';
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

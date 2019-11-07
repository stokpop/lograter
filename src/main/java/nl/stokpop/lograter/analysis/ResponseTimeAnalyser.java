/*
 * Copyright (C) 2019 Peter Paul Bakker, Stokpop Software Solutions
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

import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.util.metric.MetricPoint;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.util.List;

public interface ResponseTimeAnalyser {
    String getCounterKey();

    RequestCounter getCounter();

    long percentileHitDuration(double percentile);

    TransactionCounterResult maxHitsPerSecond();

    TransactionCounterResult maxHitsPerMinute();

    TransactionCounterResult maxHitsPerMinuteWithTpsMeasurements();

    TransactionCounterResult maxHitsPerSecondWithTpsMeasurements();

    TransactionCounterResult maxHitsPerHour();

    @SuppressWarnings("boxing")
    TransactionCounterResult maxHitsPerDuration(long durationInMillis);

    @SuppressWarnings("boxing")
    TransactionCounterResult maxHitsPerDuration(
            long timeBucketPeriod,
            boolean includeTpsMeasurements);

    ConcurrentCounterResult maxConcurrentRequests();

    double avgTps();

    double durationInHours();

    HistogramData histogramForRelevantValues(int graphHistoNumberOfRanges);

    List<MetricPoint> metricPoints();

    double percentage(long overallTotalHits);

    double stdDevHitDuration();

    long hitsInMinuteWithStartTime(long startTimeStamp);

    long totalHits();

    double avgHitDuration();

    long min();

    long max();

    long percentilePlus(double percentile);

    long[] percentiles();

    long[] percentiles(int highest);

    TimePeriod getAnalysisTimePeriod();

    /**
     * @return true if there are any hits, failed or success.
     */
    boolean hasAnyHits();
}

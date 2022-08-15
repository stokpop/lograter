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
package nl.stokpop.lograter.processor.performancecenter;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.util.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PerformanceCenterCalculator {

    private static final Logger log = LoggerFactory.getLogger(PerformanceCenterCalculator.class);

    private PerformanceCenterCalculator() {}
    
    /**
     * @return a list of durations in seconds containing the max and min values and the remaining avg values.
     */
	public static List<Double> createDataSet(int count, double min, double max, double avg) {
        List<Double> values = new ArrayList<>();
		values.add(min);
		values.add(max);

		final int countMinusMinMax = count - 2;

		final double remainingSum = (avg * count) - min - max;
		final double remainingAvg = remainingSum / countMinusMinMax;

		for (int i = 0; i < countMinusMinMax; i++) {
			values.add(remainingAvg);
		}

		Collections.shuffle(values);
		return values;
	}

    /**
     * Find aggregation period for database.
     * @param resultsDatabaseFile the full path to the db file, this method will look for aggregation info in same directory
     *                            as this file.
     * @return null when the aggregation period cannot be determined, otherwise the aggregation period in seconds
     */
    public static Integer determineAggregationPeriod(File resultsDatabaseFile) {
        int aggregationPeriodInSeconds;
        try {
             aggregationPeriodInSeconds = PerformanceCenterFileParser.fetchAnalysisAggregationPeriodInSeconds(resultsDatabaseFile.getAbsoluteFile().getParentFile());
        } catch (IOException | LogRaterException e) {
            log.warn("Unable to read aggregation period from lra file: {}", e.getMessage());
            return null;
        }
        return aggregationPeriodInSeconds;
    }

    public static long calculateLocalStartTimeSecEpoch(long testStartTimeSecEpoch, long timeZoneOffset) {

        final long startTimeMs = testStartTimeSecEpoch * 1000;
        final ZoneId zoneId = ZoneId.systemDefault();
        final boolean isDayLightSavingActive = DateUtils.isDayLightSavingActive(startTimeMs, zoneId);
        // the offset is bigger because it seems HP is adding DST to epoch time???
        final long dayLightSavingsOffset = isDayLightSavingActive ? -3600 : 0;

        final long localStartTimeSecEpoch = testStartTimeSecEpoch + dayLightSavingsOffset;

        log.info("In [{}] {}: " +
                        "StartTimeSecEpoch [{}] timeZoneOffset [{}] makes start time of test data [{}] ([{}] seconds since epoch)",
                zoneId.getId(),
                isDayLightSavingActive ? "summer time (daylight saving time)   " : "winter time (no daylight saving time)",
                testStartTimeSecEpoch,
                timeZoneOffset,
                DateUtils.formatToStandardDateTimeString(localStartTimeSecEpoch * 1000),
                localStartTimeSecEpoch);

        return localStartTimeSecEpoch;
	}

    public static double calculateGranularitySec(Double endTimeFirst, Double endTimeSecond) {
        return endTimeSecond - endTimeFirst;
	}
}

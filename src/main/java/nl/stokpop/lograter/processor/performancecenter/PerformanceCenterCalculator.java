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
package nl.stokpop.lograter.processor.performancecenter;

import nl.stokpop.lograter.LogRaterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
        try {
            return ResultConfigReader
                    .read(resultsDatabaseFile.getAbsoluteFile().getParentFile())
                    .getAggSecGran();
        } catch (IOException e) {
            log.warn("Unable to read aggregation period from lra file: {}", e.getMessage());
            return null;
        }
    }

    public static long calculateStartTimeSecEpoch(File resultsDatabaseFile) {
        try {
            ResultConfig resultConfig = ResultConfigReader.read(resultsDatabaseFile.getAbsoluteFile().getParentFile());
            String resultConfigFileName = resultConfig.getConfigPath().getFileName().toString();
            log.info(
                    "Start time (UTC): <{}>.ScenarioStartTime - <{}>.DaylightSavingSecsAddition",
                    resultConfigFileName, resultConfigFileName
            );
            return resultConfig.getScenarioStartTime() - resultConfig.getDaylightSavingSecsAddition();
        } catch (IOException e) {
            log.warn("Unable to read start time from lra file: {}", e.getMessage());
            throw new LogRaterException("");
        }
    }

    public static double calculateGranularitySec(Double endTimeFirst, Double endTimeSecond) {
        return endTimeSecond - endTimeFirst;
	}
}

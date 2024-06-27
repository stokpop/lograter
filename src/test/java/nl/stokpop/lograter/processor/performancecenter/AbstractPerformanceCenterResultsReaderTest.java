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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractPerformanceCenterResultsReaderTest {

    @Test
    public void calculateLocalStartTimeInSecondsEpochWinterTime() {
        // LRE logic for time in the results db uses zone-based time instead of UTC.
        // Same table also contains time zone offset.

        // The actual start time is 14:34:47 "Europe/Amsterdam" zone time (winter time)
        // date --date=@1512653687
        // Thu Dec  7 14:34:47 CET 2017

        final long startTimeInSecondsEpoch = 1512653687;
        final long timeZoneOffset = -3600;
        final long expectedResult = startTimeInSecondsEpoch + timeZoneOffset;
        long startTime = PerformanceCenterCalculator.calculateLocalStartTimeSecEpoch(startTimeInSecondsEpoch, timeZoneOffset);
        assertEquals(expectedResult, startTime);
    }

    @Test
    public void calculateLocalStartTimeInSecondsEpochSummerTime() {
        // The actual start time is 1:27:22 "Europe/Amsterdam" zone time (summer time)
        // date --date=@1508887642
        // Wed Oct  25 1:27:22 DST 2017

        final long startTimeInSecondsEpoch = 1508887642;
        final long timeZoneOffset = -3600;
        final long dayLightTimeOffset = -3600;
        final long expectedResult = startTimeInSecondsEpoch + timeZoneOffset + dayLightTimeOffset;
        long startTime = PerformanceCenterCalculator.calculateLocalStartTimeSecEpoch(startTimeInSecondsEpoch, timeZoneOffset);
        assertEquals(expectedResult, startTime);
    }
}
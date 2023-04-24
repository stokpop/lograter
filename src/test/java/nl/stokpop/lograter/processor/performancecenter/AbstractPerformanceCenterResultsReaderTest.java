/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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
        // HP logic for time in the results db seems to use GMT instead of actual timezone
        // to get to local time.

        // The actual start time is 14:34:47 local time (winter time)
        // date --date=@1512653687
        // Thu Dec  7 14:34:47 CET 2017

        final long startTimeInSecondsEpoch = 1512653687;
        final long timeZoneOffset = -3600;
        long startTime = PerformanceCenterCalculator.calculateLocalStartTimeSecEpoch(startTimeInSecondsEpoch, timeZoneOffset);
        assertEquals(startTimeInSecondsEpoch, startTime);

    }

    @Test
    public void calculateLocalStartTimeInSecondsEpochWinterTime2() {
        //	Analysis Summary Period: 10/11/2017 15:27:48 - 10/11/2017 15:31:39
        //Result ID	Scenario Name	Result Name	Time Zone	Start Time	Result End Time
        //0	Scenario1	res1696.lrr	-3600	1510324068	1510324299
        //	ScenarioTimeZone=-3600
        //	DaylightSavingSecsAddition=0
        //	ScenarioStartTime=1510324068
        //	ScenarioEndTime=1510324300

        final long startTimeInSecondsEpoch = 1510324068;
        final long timeZoneOffset = -3600;
        long startTime = PerformanceCenterCalculator.calculateLocalStartTimeSecEpoch(startTimeInSecondsEpoch, timeZoneOffset);
        assertEquals(startTimeInSecondsEpoch, startTime);
    }

    @Test
    public void calculateLocalStartTimeInSecondsEpochSummerTime() {
        //Analysis Summary Period: 25-10-2017 00:27:22 - 25-10-2017 00:39:57
        // result.mdb / result
        //Result ID	Scenario Name	Result Name	Time Zone	Start Time	Result End Time
        //0	Scenario1	res290.lrr	-3600	1508887642	1508888397
        //	ScenarioTimeZone=-3600
        //	DaylightSavingSecsAddition=3600
        //	ScenarioStartTime=1508887642
        //	ScenarioEndTime=1508888398

        // date --date @1508887642
        // Wed Oct 25 01:27:22 CEST 2017

        // date --date @1508884042
        // Wed Oct 25 00:27:22 CEST 2017

        final long startTimeInSecondsEpoch = 1508887642;
        final long timeZoneOffset = -3600;
        long startTime = PerformanceCenterCalculator.calculateLocalStartTimeSecEpoch(startTimeInSecondsEpoch, timeZoneOffset);
        assertEquals(startTimeInSecondsEpoch - 3600, startTime);
    }
}
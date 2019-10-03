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
package nl.stokpop.lograter.util.time;

import nl.stokpop.lograter.LogRaterException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimePeriodTest {

    @Test
    public void testNoOverlap() {
        TimePeriod timePeriodOne = TimePeriod.createExcludingEndTime(1, 2000);
        TimePeriod timePeriodTwo = TimePeriod.createExcludingEndTime(2001, 3000);

        assertFalse("One does not overlap two", timePeriodOne.overlaps(timePeriodTwo));
        assertFalse("One does not overlap two", timePeriodTwo.overlaps(timePeriodOne));
    }

    @Test
    public void testOverlap() {
        TimePeriod timePeriodOne = TimePeriod.createIncludingEndTime(1, 2000);
        TimePeriod timePeriodTwo = TimePeriod.createExcludingEndTime(2000, 3000);

        assertTrue("One does overlap two", timePeriodOne.overlaps(timePeriodTwo));
        assertTrue("One does overlap two", timePeriodTwo.overlaps(timePeriodOne));
    }

    @Test
    public void testOverlapComplete() {
        TimePeriod timePeriodOne = TimePeriod.createIncludingEndTime(1, 2000);
        TimePeriod timePeriodTwo = TimePeriod.createExcludingEndTime(10, 1990);

        assertTrue("One does overlap two", timePeriodOne.overlaps(timePeriodTwo));
        assertTrue("One does overlap two", timePeriodTwo.overlaps(timePeriodOne));
    }

    @Test
    public void testNoOverlapWithUnset() {
        TimePeriod timePeriodOne = TimePeriod.createIncludingEndTime(TimePeriod.NOT_SET, TimePeriod.NOT_SET);
        TimePeriod timePeriodTwo = TimePeriod.createExcludingEndTime(2000, 3000);

        assertFalse("One does not overlap when boundaries are unspecified.", timePeriodOne.overlaps(timePeriodTwo));
    }

    @Test
    public void testCovers() {
        TimePeriod timePeriodBig = TimePeriod.createExcludingEndTime(1, 2000);
        TimePeriod timePeriodSmall = TimePeriod.createExcludingEndTime(100, 200);

        assertTrue("Small covers small", timePeriodSmall.covers(timePeriodSmall));
        assertTrue("Big covers small", timePeriodBig.covers(timePeriodSmall));
        assertFalse("Small does not cover big", timePeriodSmall.covers(timePeriodBig));
    }

    @Test
    public void testCoversWithSlack() {
        TimePeriod timePeriodBig = TimePeriod.createExcludingEndTime(1, 2000);
        TimePeriod timePeriodSmall = TimePeriod.createExcludingEndTime(5, 1995);

        assertTrue("Small covers small", timePeriodSmall.covers(timePeriodSmall, 10));
        assertTrue("Big covers small", timePeriodBig.covers(timePeriodSmall, 10));
        assertTrue("Small covers big with slack", timePeriodSmall.covers(timePeriodBig, 10));
        assertFalse("Small does not covers big without slack", timePeriodSmall.covers(timePeriodBig));
        assertFalse("Small does not covers big with too little slack", timePeriodSmall.covers(timePeriodBig, 4));
    }

    @Test
    public void testIsWithinTimePeriod() {
        TimePeriod timePeriod = TimePeriod.createExcludingEndTime(0, 878);
        assertTrue(timePeriod.isWithinTimePeriod(5));
        assertTrue(timePeriod.isWithinTimePeriod(0));
        assertFalse(timePeriod.isWithinTimePeriod(878));
    }

    @Test
    public void testHumanReadableDurationMillis() {
        TimePeriod timePeriod = TimePeriod.createExcludingEndTime(0, 878);
        assertEquals("878 milliseconds", timePeriod.getHumanReadableDuration());
    }

    @Test
    public void testHumanReadableDurationSeconds() {
        TimePeriod timePeriod = TimePeriod.createExcludingEndTime(10000, 23480);
        assertEquals("13 seconds, 480 milliseconds", timePeriod.getHumanReadableDuration());
    }

    @Test
    public void testHumanReadableDurationMinutes() {
        TimePeriod timePeriod = TimePeriod.createExcludingEndTime(10000, 123480);
        assertEquals("1 minute, 53 seconds, 480 milliseconds", timePeriod.getHumanReadableDuration());
    }

	@Test
	public void durationOfOnePointIsOneMs() {
		TimePeriod timePeriod = TimePeriod.createExcludingEndTime(10_000, 10_001);
		assertEquals(1, timePeriod.getDurationInMillis());
	}

    @Test
    public void testHumanReadableDurationHours() {
        TimePeriod timePeriod = TimePeriod.createExcludingEndTime(0, (10 * 60 * 60 * 1000) + 1);
        assertEquals("10 hours, 1 millisecond", timePeriod.getHumanReadableDuration());
    }

    @Test
    public void testHumanReadableDurationTooLong() {
        TimePeriod timePeriod = TimePeriod.createExcludingEndTime(0, ((long) Integer.MAX_VALUE) + 1);
        String humanReadableDurationTooLong = timePeriod.getHumanReadableDuration();
        System.out.println(humanReadableDurationTooLong);
        assertTrue(humanReadableDurationTooLong.startsWith("Longer than"));
    }

	@Test(expected = LogRaterException.class)
	public void testHumanReadableDurationStartNotSet() {
		TimePeriod.createExcludingEndTime(TimePeriod.NOT_SET, 1000);
	}

	@Test(expected = LogRaterException.class)
    public void testHumanReadableDurationEndNotSet() {
        TimePeriod.createExcludingEndTime(1000, TimePeriod.NOT_SET);
    }

}
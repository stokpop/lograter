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

import net.jcip.annotations.Immutable;
import nl.stokpop.lograter.LogRaterException;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

@Immutable
public final class TimePeriod {

    public final static long MIN = 0L;
    public final static long MAX = Long.MAX_VALUE;
    public final static long NOT_SET = -1L;
    public final static String NOT_SET_MESSAGE = "not set";

    private final long startTime;
    private final long endTime;

    private final long durationInMillis;

    private static final Period MAX_PERIOD_FOR_INT = new Period(Integer.MAX_VALUE);

    public static final TimePeriod MAX_TIME_PERIOD = new TimePeriod(MIN, MAX);
    public static final TimePeriod MIN_TIME_PERIOD = new TimePeriod(0, 1);
	public static final TimePeriod UNDEFINED_PERIOD = new TimePeriod(NOT_SET, NOT_SET);

    private static final PeriodFormatter daysHoursMinutes = new PeriodFormatterBuilder()
            .appendDays()
            .appendSuffix(" day", " days")
            .appendSeparator(", ")
            .appendHours()
            .appendSuffix(" hour", " hours")
            .appendSeparator(", ")
            .appendMinutes()
            .appendSuffix(" minute", " minutes")
            .appendSeparator(", ")
            .appendSeconds()
            .appendSuffix(" second", " seconds")
            .appendSeparator(", ")
            .appendMillis()
            .appendSuffix(" millisecond", " milliseconds")
            .toFormatter();

    /**
     * Use NOT_SET in case the start and/or end is not set.
     * TimePeriod means larger or equal to start time and less then end time.
     */
    private TimePeriod(final long startTimestamp, final long endTimestamp) {
	    if (startTimestamp == NOT_SET && endTimestamp == NOT_SET) {
		    this.startTime = NOT_SET;
		    this.endTime = NOT_SET;
		    this.durationInMillis = NOT_SET;
		    return;
	    }
	    if (startTimestamp == NOT_SET || endTimestamp == NOT_SET) {
		    throw new LogRaterException(String.format("TimePeriods with one timestamp not set are not supported. Startime [%d] Endtime [%d].", startTimestamp, endTimestamp));
	    }
        validateTimestamp(startTimestamp);
        validateTimestamp(endTimestamp);
        if (endTimestamp < startTimestamp) {
            throw new LogRaterException(String.format("Start time should be before or equal to end time: [%d, %d].", startTimestamp, endTimestamp));
        }
        this.startTime = startTimestamp;
        this.endTime = endTimestamp;
	    this.durationInMillis = endTimestamp - startTimestamp;
    }

    /**
     * @return timeperiod where last timestamp is not included in duration calculation. E.g. duration of (1,1) is 0 ms.
     */
    public static TimePeriod createExcludingEndTime(final long startTimestamp, final long endTimestamp) {
    	return new TimePeriod(startTimestamp, endTimestamp);
    }

    /**
     * @return timeperiod where last timestamp is included in duration calculation. E.g. duration of (1,1) is 1 ms.
     * Note this is internally done by adding one ms to the endTimestamp int this TimePeriod.
     */
	public static TimePeriod createIncludingEndTime(final long startTimestamp, final long endTimestamp) {
	    if (startTimestamp == NOT_SET && endTimestamp == NOT_SET) { return UNDEFINED_PERIOD; }
		return new TimePeriod(startTimestamp, endTimestamp + 1);
	}

	/**
	 * Create a time period as large as possible from combining the two time periods.
	 */
	public static TimePeriod createMaxTimePeriod(TimePeriod one, TimePeriod two) {

	    final boolean bothStartNotSet = !one.isStartTimeSet() && !two.isStartTimeSet();
	    final boolean bothEndNotSet = !one.isEndTimeSet() && !two.isEndTimeSet();

        final long startTime;
	    if (bothStartNotSet) {
	        startTime = TimePeriod.NOT_SET;
        }
        else {
            long startTimeOne = one.isStartTimeSet() ? one.getStartTime() : Long.MAX_VALUE;
            long startTimeTwo = two.isStartTimeSet() ? two.getStartTime() : Long.MAX_VALUE;
            startTime = Math.min(startTimeOne, startTimeTwo);
        }

        final long endTime;
	    if (bothEndNotSet) {
	        endTime = TimePeriod.NOT_SET;
        }
        else {
            long endTimeOne = one.isEndTimeSet() ? one.getEndTime() : 0;
            long endTimeTwo = two.isEndTimeSet() ? two.getEndTime() : 0;
            endTime = Math.max(endTimeOne, endTimeTwo);
        }

		return bothStartNotSet && bothEndNotSet ? TimePeriod.UNDEFINED_PERIOD : new TimePeriod(startTime, endTime);
	}

	private void validateTimestamp(long timestamp) {
        if (timestamp < 0) {
            throw new LogRaterException("Invalid timestamp: " + timestamp);
        }
    }

    public long getStartTime() {
        return this.startTime;
    }

    public boolean isStartTimeSet() {
        return startTime != NOT_SET;
    }

    public boolean isEndTimeSet() {
        return endTime != NOT_SET;
    }

	/**
	 * @return true when timestamp equal or higher that start time and lower than end time.
	 */
	public boolean isWithinTimePeriod(long timestamp) {
	    // when both start en endtime are not set, return true, always.
	    return !hasBothTimestampsSet() || timestamp >= startTime && timestamp < endTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    @Override
    public String toString() {
        return "TimePeriod{" +
                "start=" + getHumanReadableStartTimestamp() +
                ", end=" + getHumanReadableEndTimestamp() +
                ", duration=" + getHumanReadableDuration() +
                '}';
    }

    public String toSimpleString() {
        return "TimePeriod{" +
                "start=" + this.startTime +
                ", end=" + this.endTime +
                ", duration=" + this.durationInMillis +
                '}';
    }

    public String getHumanReadableStartTimestamp() {
        return isStartTimeSet() ? DateUtils.formatToHumanReadableTimeStringWithMillis(startTime) : NOT_SET_MESSAGE;
    }

    public String getHumanReadableEndTimestamp() {
        return isEndTimeSet() ? DateUtils.formatToHumanReadableTimeStringWithMillis(endTime) : NOT_SET_MESSAGE;
    }

    /**
     * If given filterPeriod has its start and/or endtime set, use the filter timestamps.
     * @return a new TimePeriod with filters set if applicable, otherwise this is returned, or null if null is given.
     */
    public TimePeriod createFilterTimePeriodIfFilterIsSet(TimePeriod filterPeriod) {
        if (!filterPeriod.isStartTimeSet() && !filterPeriod.isEndTimeSet()) {
            return this;
        }
        long startTime = filterPeriod.isStartTimeSet() ? filterPeriod.getStartTime() : getStartTime();
        long endTime = filterPeriod.isEndTimeSet() ? filterPeriod.getEndTime() : getEndTime();
        return new TimePeriod(startTime, endTime);
    }

    public long getDurationInMillis() {
        return durationInMillis;
    }

    public String getHumanReadableDuration() {
        if (this.startTime == NOT_SET) {
            return "No start time set, unknown duration: " + toSimpleString();
        }
        if (this.endTime == NOT_SET) {
            return "No end time set, unknown duration: " + toSimpleString();
        }
        if (durationInMillis > Integer.MAX_VALUE) {
            return "Longer than " + daysHoursMinutes.print(MAX_PERIOD_FOR_INT) + " (max printable period in Joda time): " + toSimpleString();
        }
        else {
            Period period = new Period(durationInMillis);
            return daysHoursMinutes.print(period);
        }
    }

    /**
     * Check if two periods cover each other completely.
     * @param slackInMillis forgive if begin or end time is this many millis apart
     * @return true when there is one or more timestamps in both time periods
     */
    public boolean covers(TimePeriod other, long slackInMillis) {
        if (this.isUndefined() || other.isUndefined()) { return false; }

        return other.getEndTime() - slackInMillis <= this.endTime && other.getStartTime() + slackInMillis >= this.startTime;

    }

    /**
     * Check if two periods overlap each other.
     * @return true when there is one or more timestamps in both time periods
     */
    public boolean overlaps(TimePeriod other) {
        if (this.isUndefined() || other.isUndefined()) { return false; }
        return this.isWithinTimePeriod(other.startTime)
                || this.isWithinTimePeriod(other.endTime)
                || other.isWithinTimePeriod(this.startTime)
                || other.isWithinTimePeriod(this.endTime);
    }

    /**
     * @see #covers(TimePeriod, long) with slack 0.
     */
    public boolean covers(TimePeriod other) {
        return covers(other, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimePeriod that = (TimePeriod) o;

        return startTime == that.startTime && endTime == that.endTime;

    }

    @Override
    public int hashCode() {
        int result = (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (endTime ^ (endTime >>> 32));
        return result;
    }

    public double getDurationInSeconds() {
        return ((double) getDurationInMillis()) / 1000;
    }

	public boolean hasBothTimestampsSet() {
		return startTime != NOT_SET && endTime != NOT_SET;
	}

	public boolean isUndefined() {
		return startTime == NOT_SET && endTime == NOT_SET;
	}

}

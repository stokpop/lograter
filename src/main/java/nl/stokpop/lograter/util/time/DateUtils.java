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
package nl.stokpop.lograter.util.time;

import nl.stokpop.lograter.LogRaterException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.regex.Pattern;

public class DateUtils {

    private static final Logger log = LoggerFactory.getLogger(DateUtils.class);

	public static final Locale DEFAULT_LOCALE = Locale.US;
	
	private static final String STD_DATETIME_FORMAT = "yyyyMMdd'T'HHmmss";
	private static final DateTimeFormatter STD_DATETIME_FORMATTER = DateTimeFormat.forPattern(STD_DATETIME_FORMAT).withLocale(DEFAULT_LOCALE);
	private static final DateTimeFormatter STD_TIME_FORMATTER = DateTimeFormat.forPattern("dd-MM HH:mm:ss").withLocale(DEFAULT_LOCALE);

	private static final DateTimeFormatter HUMAN_READABLE_WITH_MILLIS = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss.SSS").withLocale(DEFAULT_LOCALE);

	private static final Pattern PATTERN_TIMESTAMP = Pattern.compile("\\{ts}");

	public static boolean isDayLightSavingActive(Instant date, ZoneId zoneId) {
	    return zoneId.getRules().isDaylightSavings(date);
    }

	public static boolean isDayLightSavingActive(long epochMillis, ZoneId zoneId) {
	    return isDayLightSavingActive(Instant.ofEpochMilli(epochMillis), zoneId);
    }

	public static boolean isValidDateTimeString(String dateTimeString) {
		try {
			parseStandardDateTime(dateTimeString);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

	public static long parseStandardDateTime(String timeString) {
		return STD_DATETIME_FORMATTER.parseMillis(timeString);
	}

	public static String formatToStandardDateTimeString(long time) {
	    return STD_DATETIME_FORMATTER.print(time);
	}

	public static long parseStandardTime(String timeString) {
		return STD_TIME_FORMATTER.parseMillis(timeString);
	}

	public static String formatToStandardTimeString(long time) {
	    return STD_TIME_FORMATTER.print(time);
	}

	public static String formatToHumanReadableTimeStringWithMillis(long time) {
	    return HUMAN_READABLE_WITH_MILLIS.print(time);
	}

	public static String appendSecondsIfNeeded(String timestring) {
		return timestring.length() == 13 ? timestring + "00" : timestring;
	}

    public static String replaceTimestampMarkerInFilename(String filename, long timestamp) {
        return filename == null ? null : PATTERN_TIMESTAMP.matcher(filename).replaceAll(formatToStandardDateTimeString(timestamp));
    }

    public static String replaceTimestampMarkerInFilename(String outputFilename) {
        return replaceTimestampMarkerInFilename(outputFilename, System.currentTimeMillis());
    }

    /**
     * Convert a strftime pattern to a Joda DateTime pattern.
     * See: http://man7.org/linux/man-pages/man3/strftime.3.html and http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
     * @param strfTimePattern a pattern that follows the stftime format (e.g. from apache log format)
     * @return the pattern used in joda time date parsing
     */
	public static String convertStrfTimePatternToDateTimeFormatterPattern(String strfTimePattern) {
        StringBuilder dateTimePattern = new StringBuilder();

        boolean isVariable = false;
        for (int i = 0; i < strfTimePattern.length(); i++) {
            char c = strfTimePattern.charAt(i);
            if (isVariable) {
                if (c == '%') {
                    dateTimePattern.append(c);
                }
                else {
                    dateTimePattern.append(convertToDateTimePattern(c));
                }
                isVariable = false;
            }
            else if (c == '%') {
                isVariable = true;
            }
            else {
                dateTimePattern.append(c);
            }
        }

		return dateTimePattern.toString();
	}

    private static String convertToDateTimePattern(char c) {
        switch(c) {
            case 'd': return "dd";
            case 'D': return "MM/dd/yy";
            case 'b': return "MMM";
            case 'B': return "MMMM";
            case 'Y': return "yyyy";
            case 'y': return "yy";
            case 'H': return "HH";
            case 'h': return "hh";
            case 'I': return "hh";
            case 'M': return "mm";
            case 'm': return "MM";
            case 'S': return "ss";
            case 'a': return "EEE";
            case 'A': return "EEEE";
            case 'c': return "EEE MMM dd HH:mm:ss yyyy";
            case 'e': return "d";
            case 'f': return "SSSS";
            case 'j': return "DDD";
            case 'k': return "H";
            case 'l': return "h";
            case 'p': return "a";
            case 'U': return "ww";
            case 'w': return "uu";
            case 'W': return "ww";
            case 'x': return "MM/dd/yy";
            case 'X': return "HH:mm:ss";
            case 'z': return "Z";
            case 'Z': return "ZZZ";
            case 'T': return "HH:mm:ss";
            case 't': return "\t";
            case 'F': return "HH-mm-ss";

            default: return String.valueOf(c);
        }
    }

	/**
	 * Based on the start time and end time a TimePeriod is created. If both are null then an undefined period is returned.
	 */
	public static TimePeriod createFilterPeriod(String startTimeAsString, String endTimeAsString) {

		boolean startTimePresent = startTimeAsString != null;
		boolean endTimePresent = endTimeAsString != null;

		if (!startTimePresent || !endTimePresent) {
			log.warn("No start time and or end time present on command line. Using dynamic time period for analysis.");
			return TimePeriod.UNDEFINED_PERIOD;
		}

		long startTime;
		long endTime;

		String startTimeStr = appendSecondsIfNeeded(startTimeAsString);
		boolean validDateTimeString = isValidDateTimeString(startTimeStr);
		if (validDateTimeString) {
			startTime = parseStandardDateTime(startTimeStr);
		}
		else {
			throw new LogRaterException(String.format("Invalid start time: %s", startTimeStr));
		}

		String endTimeStr = appendSecondsIfNeeded(endTimeAsString);
		if (isValidDateTimeString(endTimeStr)) {
			endTime = parseStandardDateTime(endTimeStr);
		}
		else {
			throw new LogRaterException(String.format("Invalid end time: %s", startTimeStr));
		}
		return TimePeriod.createExcludingEndTime(startTime, endTime);
	}
}
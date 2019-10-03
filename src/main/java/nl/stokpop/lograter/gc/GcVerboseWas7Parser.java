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
package nl.stokpop.lograter.gc;

import nl.stokpop.lograter.util.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse verbose gc for WAS 7
 */
public class GcVerboseWas7Parser {

    private static final Logger log = LoggerFactory.getLogger(GcVerboseWas7Parser.class);

    private static final Pattern sysDumpPattern = Pattern
            .compile("<sys id=\"([0-9]+)\" timestamp=\"(.*)\" intervalms=\"[0-9.]+\">");

    private static final Pattern afNurseryPattern = Pattern
            .compile("<af type=\"nursery\" id=\"([0-9]+)\" timestamp=\"(.*)\" intervalms=\"[0-9.]+\">");

    private static final Pattern afTenuredPattern = Pattern
            .compile("<af type=\"tenured\" id=\"([0-9]+)\" timestamp=\"(.*)\" intervalms=\"[0-9.]+\">");

    private static final Pattern globalCollectionPattern = Pattern
            .compile("<gc type=\"global\" id=\"([0-9]+)\" totalid=\"[0-9.]+\" intervalms=\"[0-9.]+\">");

    private static final Pattern concurrentCollectionPattern = Pattern
            .compile("<con event=\"collection\" id=\"([0-9]+)\" timestamp=\"(.*)\" intervalms=\"[0-9.]+\">");

    private static final Pattern nurseryPattern = Pattern
            .compile("<nursery freebytes=\"([0-9]+)\" totalbytes=\"([0-9]+)\" percent=\"[0-9.]+\" />");

    private static final Pattern tenuredPattern = Pattern
            .compile("<tenured freebytes=\"([0-9]+)\" totalbytes=\"([0-9]+)\" percent=\"[0-9.]+\" >");

    // use the 2nd occurrance
    private static final Pattern timeTotalmsPattern = Pattern
            .compile("<time totalms=\"([\\.0-9]+)\" />");

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MMM dd HH:mm:ss yyyy").withLocale(new Locale("US"));

    public List<GcLogEntry> analyse(File gcFile) throws IOException {
        List<GcLogEntry> entries = new ArrayList<>();

        try (BufferedReader gcFileInput = FileUtils.getBufferedReader(gcFile)) {
            String line;
            boolean isInCollectionEntry = false;

            int nurseryOccurrence = 0;
            boolean lookForTenured = false;
            boolean lookForTimeMs = false;
            boolean complete = false;
            GcLogEntry.GcLogEntryBuilder entryBuilder = new GcLogEntry.GcLogEntryBuilder();

            int GROUP_ID = 1;
            int GROUP_TIMESTAMP = 2;
            int GROUP_FREEBYTES = 1;
            int GROUP_TOTALBYTES = 2;
            int GROUP_TOTALMS = 1;

            while ((line = gcFileInput.readLine()) != null) {

                if (complete) {
                    GcLogEntry newEntry = entryBuilder.createGcLogEntry();
                    log.debug("Adding gc log entry [{}]", newEntry);
                    entries.add(newEntry);
                    entryBuilder = new GcLogEntry.GcLogEntryBuilder();
                    isInCollectionEntry = false;
                    nurseryOccurrence = 0;
                    lookForTenured = false;
                    lookForTimeMs = false;
                    complete = false;
                }

                if(!isInCollectionEntry) {
                    Matcher afNurseryMatcher = afNurseryPattern.matcher(line);
                    if (afNurseryMatcher.find()) {
                        isInCollectionEntry = true;
                        entryBuilder.setGcType(GcType.NURSERY);
                        entryBuilder.setGcReason(GcReason.AF);
                        entryBuilder.setId(Integer.parseInt(afNurseryMatcher.group(GROUP_ID)));
                        DateTime dateTime = dateTimeFormatter.parseDateTime(afNurseryMatcher.group(GROUP_TIMESTAMP));
                        entryBuilder.setTimestamp(dateTime.getMillis());
                    }
                    if (!isInCollectionEntry) {
                        Matcher sysMatcher = sysDumpPattern.matcher(line);
                        if (sysMatcher.find()) {
                            isInCollectionEntry = true;
                            entryBuilder.setGcReason(GcReason.SYS);
                            entryBuilder.setGcType(GcType.GLOBAL);
                            entryBuilder.setId(Integer.parseInt(sysMatcher.group(GROUP_ID)));
                            DateTime dateTime = dateTimeFormatter.parseDateTime(sysMatcher.group(GROUP_TIMESTAMP));
                            entryBuilder.setTimestamp(dateTime.getMillis());
                        }
                    }
                    if (!isInCollectionEntry) {
                        Matcher concMatcher = concurrentCollectionPattern.matcher(line);
                        if (concMatcher.find()) {
                            isInCollectionEntry = true;
                            entryBuilder.setGcType(GcType.GLOBAL);
                            entryBuilder.setGcReason(GcReason.CON);
                            final String group = concMatcher.group(GROUP_ID);
                            entryBuilder.setId(Integer.parseInt(group));
                            DateTime dateTime = dateTimeFormatter.parseDateTime(concMatcher.group(GROUP_TIMESTAMP));
                            entryBuilder.setTimestamp(dateTime.getMillis());
                        }
                    }
                    if (!isInCollectionEntry) {
                        Matcher afTenuredMatcher = afTenuredPattern.matcher(line);
                        if (afTenuredMatcher.find()) {
                            isInCollectionEntry = true;
                            entryBuilder.setGcType(GcType.GLOBAL);
                            entryBuilder.setGcReason(GcReason.AF);
                            entryBuilder.setId(Integer.parseInt(afTenuredMatcher.group(GROUP_ID)));
                            DateTime dateTime = dateTimeFormatter.parseDateTime(afTenuredMatcher.group(GROUP_TIMESTAMP));
                            entryBuilder.setTimestamp(dateTime.getMillis());
                        }
                    }
                }
                else {
                    Matcher globMatcher = globalCollectionPattern.matcher(line);
                    if (globMatcher.find()) {
                        entryBuilder.setGlobalId(Integer.parseInt(globMatcher.group(GROUP_ID)));
                    }
                    if (!lookForTenured) {
                        Matcher nurseryMatcher = nurseryPattern.matcher(line);
                        if (nurseryMatcher.find()) {
                            nurseryOccurrence++;
                            // there are multiple lines with nursery and tenured size, make sure to fetch the second which is after gc
                            if (nurseryOccurrence == 2) {
                                lookForTenured = true;
                                final long nurseryFreebytes = Long.parseLong(nurseryMatcher.group(GROUP_FREEBYTES));
                                entryBuilder.setNurseryFreebytes(nurseryFreebytes);
                                final long nurseryTotalbytes = Long.parseLong(nurseryMatcher.group(GROUP_TOTALBYTES));
                                entryBuilder.setNurseryTotalbytes(nurseryTotalbytes);
                            }
                        }
                    }
                    else {
                        Matcher tenuredMatcher = tenuredPattern.matcher(line);
                        if (tenuredMatcher.find()) {
                            lookForTimeMs = true;
                            final long tenuredFreebytes = Long.parseLong(tenuredMatcher.group(GROUP_FREEBYTES));
                            entryBuilder.setTenuredFreebytes(tenuredFreebytes);
                            final long tenuredTotalbytes = Long.parseLong(tenuredMatcher.group(GROUP_TOTALBYTES));
                            entryBuilder.setTenuredTotalbytes(tenuredTotalbytes);
                        }
                    }
                    if (lookForTimeMs) {
                        // this is supposed to be the second occurrence, because nursery memory count is already 2
                        Matcher timeMatcher = timeTotalmsPattern.matcher(line);
                        if (timeMatcher.find()) {
                            complete = true;
                            final double durationMs = Double.parseDouble(timeMatcher.group(GROUP_TOTALMS));
                            entryBuilder.setExclusiveDurationMs(durationMs);
                        }
                    }
                }
            }
        }

        return entries;
    }
}

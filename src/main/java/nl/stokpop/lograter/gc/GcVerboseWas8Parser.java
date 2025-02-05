/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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
 * Parse verbose gc for WAS 8
 */
class GcVerboseWas8Parser {

    private static final Logger log = LoggerFactory.getLogger(GcVerboseWas8Parser.class);

    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withLocale(new Locale("US"));

    //<concurrent-kickoff id="10279" timestamp="2014-08-20T23:25:47.872">
    private static final Pattern patternWas8ConcurrentKickoff =
            Pattern.compile("<concurrent-kickoff id=\"([0-9]+)\" timestamp=\"(.*)\"");

    // <cycle-start id="83520" type="global" contextid="0" timestamp="2014-01-30T22:14:24.460" intervalms="1426291.011" />
    private static final Pattern patternWas8cycleStartGlobal =
            Pattern.compile("<cycle-start id=\"([0-9]+)\" type=\"global\" contextid=\"[0-9]+\" timestamp=\"(.*)\" intervalms=\"[0-9.]+\" />");

    // <concurrent-collection-start id="4333" timestamp="2014-08-20T21:12:26.582" intervalms="4860370.180" />
    private static final Pattern patternWas8concurrentCollectionStart =
            Pattern.compile("<concurrent-collection-start id=\"([0-9]+)\" timestamp=\"(.*)\" intervalms=\"[0-9.]+\" />");

    //    <gc-end id="83527" type="global" contextid="83520" durationms="104.757" timestamp="2014-01-30T22:14:24.565">
	//    was 8.5.5.10: <gc-end id="11" type="global" contextid="4" durationms="251.895" usertimems="247.962" systemtimems="2.999" timestamp="2016-10-27T13:10:10.325" activeThreads="1">
    private static final Pattern patternWas8gcEnd =
            Pattern.compile("<gc-end id=\"([0-9]+)\" type=\"(.*)\" contextid=\"([0-9]+)\" durationms=\"[0-9.]+\".* timestamp=\"(.*)\".*>");

    //    <mem-info id="83528" free="1947343704" total="2040004608" percent="95">
    private static final Pattern patternWas8memInfoTotal =
            Pattern.compile("<mem-info id=\"([0-9]+)\" free=\"([0-9]+)\" total=\"([0-9]+)\" percent=\"([0-9]+)\">");

    //    <mem type="nursery" free="957316488" total="966328320" percent="99" />
    private static final Pattern patternWas8memNursery =
            Pattern.compile("<mem type=\"nursery\" free=\"([0-9]+)\" total=\"([0-9]+)\" percent=\"([0-9]+)\".*>");

    //    <mem type="tenure" free="990027216" total="1073676288" percent="92">
    private static final Pattern patternWas8memTenure =
            Pattern.compile("<mem type=\"tenure\" free=\"([0-9]+)\" total=\"([0-9]+)\" percent=\"([0-9]+)\".*>");

    //      <sys-start id="3" timestamp="2014-08-15T19:34:58.086" intervalms="3417.370" />
    private static final Pattern patternWas8sysStart =
            Pattern.compile("<sys-start id=\"([0-9]+)\" timestamp=\"(.*)\" intervalms=\"([.0-9]+)\" />");

    //      or in new fix-pack (8.5.5.4?): <sys-start reason="explicit" id="65453" timestamp="2015-03-28T01:25:02.529" intervalms="101255.969" />
    private static final Pattern patternWas8sysStartV2 =
            Pattern.compile("<sys-start reason=\"(.*)\" id=\"([0-9]+)\" timestamp=\"(.*)\" intervalms=\"([.0-9]+)\" />");

    //      <sys-end id="14" timestamp="2014-08-15T19:34:58.231" />
    private static final Pattern patternWas8sysEnd =
            Pattern.compile("<sys-end id=\"([0-9]+)\" timestamp=\"(.*)\" />");

    //      <exclusive-end id="22220" timestamp="2014-01-30T18:20:48.451" durationms="145.038" />
    private static final Pattern patternWas8ExclusiveEnd =
            Pattern.compile("<exclusive-end id=\"([0-9]+)\" timestamp=\"(.*)\" durationms=\"([.0-9]+)\" />");

    //      <af-start id="18416" totalBytesRequested="8200" timestamp="2014-01-30T18:06:20.871" intervalms="2719.520" />
    private static final Pattern patternWas8afStart =
            Pattern.compile("<af-start id=\"([0-9]+)\" totalBytesRequested=\"([0-9]+)\" timestamp=\"(.*)\" intervalms=\"([.0-9]+)\" />");

    //      <cycle-end id="16637" type="scavenge" contextid="16631" timestamp="2014-10-29T16:37:11.112" />
    private static final Pattern patternWas8afEnd =
            Pattern.compile("<cycle-end id=\"([0-9]+)\" type=\"scavenge\" contextid=\"([0-9]+)\" timestamp=\"(.*)\" />");

    private static final int BUFFER_SIZE = 1000;

	List<GcLogEntry> extractGcLogEntries(File gcFile) throws IOException {

        List<GcLogEntry> entries = new ArrayList<>();

	    try (BufferedReader gcFileInput = FileUtils.createBufferedReader(gcFile)) {

		    String line;
		    boolean isInAfCycle = false;
		    boolean isInSysCycle = false;
		    boolean isInGlobalGcCycle = false;
		    boolean isGcEndFound = false;
		    boolean isComplete = false;
		    boolean isConcurrentKickoff = false;
		    boolean isConcurrent = false;

		    GcLogEntry.GcLogEntryBuilder builder = new GcLogEntry.GcLogEntryBuilder();

		    int GROUP_ID = 1;
		    int GROUP_TIMESTAMP = 2;
		    int GROUP_FREEBYTES = 1;
		    int GROUP_TOTALBYTES = 2;
		    int GROUP_DURATIONMS = 3;

		    while ((line = gcFileInput.readLine()) != null) {

			    if (line.trim().length() == 0) {
				    continue;
			    }

			    Matcher concurrentKickoffMatcher = patternWas8ConcurrentKickoff.matcher(line);
			    if (concurrentKickoffMatcher.find()) {
				    isConcurrentKickoff = true;
				    builder.setId(Integer.parseInt(concurrentKickoffMatcher.group(GROUP_ID)));
				    DateTime dateTime = dateTimeFormatter.parseDateTime(concurrentKickoffMatcher.group(GROUP_TIMESTAMP));
				    builder.setTimestamp(dateTime.getMillis());
				    builder.setGcReason(GcReason.CON);
				    builder.setGcType(GcType.GLOBAL);
			    }

			    if (isInGlobalGcCycle || isInAfCycle || isConcurrentKickoff) {
				    Matcher exclusiveEndMatcher = patternWas8ExclusiveEnd.matcher(line);
				    if (exclusiveEndMatcher.find()) {
					    final double durationMs = Double.parseDouble(exclusiveEndMatcher.group(GROUP_DURATIONMS));
					    builder.setExclusiveDurationMs(durationMs);
					    isComplete = true;
				    }
			    }

			    if (!isInGlobalGcCycle && !isInSysCycle) {
				    Matcher sysMatcherV2 = patternWas8sysStartV2.matcher(line);
				    if (sysMatcherV2.find()) {
					    isInSysCycle = true;
					    final String sysGcReason = sysMatcherV2.group(1);
					    builder.setSysGcReason(sysGcReason);
				    } else {
					    Matcher sysMatcher = patternWas8sysStart.matcher(line);
					    if (sysMatcher.find()) {
						    isInSysCycle = true;
						    builder.setSysGcReason("N.A.");
					    }
				    }
			    }

			    if (!isInGlobalGcCycle && !isInAfCycle) {

				    Matcher globalMatcher = patternWas8cycleStartGlobal.matcher(line);

				    if (globalMatcher.find()) {
					    isInGlobalGcCycle = true;
					    if (isInSysCycle) {
						    builder.setGcReason(GcReason.SYS);
						    builder.setGcType(GcType.GLOBAL);
					    } else {
						    builder.setGcReason(GcReason.AF);
						    builder.setGcType(GcType.NURSERY);
					    }
					    builder.setId(Integer.parseInt(globalMatcher.group(GROUP_ID)));
					    DateTime dateTime = dateTimeFormatter.parseDateTime(globalMatcher.group(GROUP_TIMESTAMP));
					    builder.setTimestamp(dateTime.getMillis());
				    }

				    Matcher conMatcher = patternWas8concurrentCollectionStart.matcher(line);
				    if (conMatcher.find()) {
					    isInGlobalGcCycle = true;
					    isConcurrent = true;
					    if (isInSysCycle) {
						    // is this even possible?
						    log.warn("Unexpected: concurrent within sys gc found: {}", line);
						    builder.setGcReason(GcReason.SYS);
						    builder.setGcType(GcType.GLOBAL);
					    } else {
						    builder.setGcReason(GcReason.CON);
						    builder.setGcType(GcType.GLOBAL);
					    }
					    builder.setId(Integer.parseInt(conMatcher.group(GROUP_ID)));
					    DateTime dateTime = dateTimeFormatter.parseDateTime(conMatcher.group(GROUP_TIMESTAMP));
					    builder.setTimestamp(dateTime.getMillis());
				    }

				    Matcher afMatcher = patternWas8afStart.matcher(line);
				    if (afMatcher.find()) {
					    isInAfCycle = true;
					    builder.setGcReason(GcReason.AF);
					    builder.setGcType(GcType.NURSERY);
					    builder.setId(Integer.parseInt(afMatcher.group(1)));
					    DateTime dateTime = dateTimeFormatter.parseDateTime(afMatcher.group(3));
					    builder.setTimestamp(dateTime.getMillis());
				    }
			    } else { // inGlobalGcCycle or inAfCycle
				    if (!isGcEndFound) {
					    Matcher gcEndMatcher = patternWas8gcEnd.matcher(line);
					    if (gcEndMatcher.find()) {
						    String type = gcEndMatcher.group(2);
						    if (type.equals("scavenge")) {
							    builder.setGcType(GcType.NURSERY);
							    builder.setGcReason(GcReason.AF);
						    } else if (type.equals("global")) {
							    if (!(isConcurrentKickoff || isConcurrent)) {
								    builder.setGcType(GcType.GLOBAL);
								    if (isInSysCycle) {
									    builder.setGcReason(GcReason.SYS);
								    } else {
									    builder.setGcReason(GcReason.AF);
								    }
							    }
						    }
						    isGcEndFound = true;
					    }
				    } else { // isGcEndFound
					    Matcher nurseryMatcher = patternWas8memNursery.matcher(line);
					    if (nurseryMatcher.find()) {
						    builder.setNurseryFreebytes(Long.parseLong(nurseryMatcher.group(GROUP_FREEBYTES)));
						    builder.setNurseryTotalbytes(Long.parseLong(nurseryMatcher.group(GROUP_TOTALBYTES)));
					    } else {
						    Matcher tenureMatcher = patternWas8memTenure.matcher(line);
						    if (tenureMatcher.find()) {
							    builder.setTenuredFreebytes(Long.parseLong(tenureMatcher.group(GROUP_FREEBYTES)));
							    builder.setTenuredTotalbytes(Long.parseLong(tenureMatcher.group(GROUP_TOTALBYTES)));
						    }
					    }
				    }
				    if (patternWas8afEnd.matcher(line).find()) {
					    gcFileInput.mark(BUFFER_SIZE);
					    String nextLine = gcFileInput.readLine();
					    // can also contain intermediate line:
					    // <percolate-collect id="102013" from="nursery" to="global" reason="failed tenure threshold reached" timestamp="2014-10-29T18:26:31.603"/>
					    String nextNextLine = gcFileInput.readLine();
					    gcFileInput.reset();

					    if (patternWas8cycleStartGlobal.matcher(nextLine).find() || patternWas8cycleStartGlobal.matcher(nextNextLine).find()) {
						    isComplete = true;
					    } else {
						    isInAfCycle = true;
					    }
				    }
			    }

			    if (isComplete) {
				    GcLogEntry newEntry = builder.createGcLogEntry();
				    log.debug("adding gc log entry: {}", newEntry);

				    if (isConcurrentKickoff) {
					    String dateTime = dateTimeFormatter.print(newEntry.getTimestamp());
					    log.debug("Concurrent kickoff found. Skipping. id: {}, timestamp: {}", newEntry.getId(), dateTime);
				    } else if (newEntry.getTotalUsedBytes() == 0) {
					    log.warn("Unexpected: gc log entry without heap size. Skipping entry. Check parser! {}: {}", builder, line);
				    } else {
					    entries.add(newEntry);
				    }
				    builder = new GcLogEntry.GcLogEntryBuilder();
				    isInGlobalGcCycle = false;
				    isGcEndFound = false;
				    isInSysCycle = false;
				    isComplete = false;
				    isInAfCycle = false;
				    isConcurrentKickoff = false;
				    isConcurrent = false;
			    }
		    }
	    }

        return entries;
    }

}

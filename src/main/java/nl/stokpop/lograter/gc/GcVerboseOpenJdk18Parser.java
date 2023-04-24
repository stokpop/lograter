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
 * Parses open jdk gc logs for Java 8. Use the following jvm options:
 * -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails
 */
public class GcVerboseOpenJdk18Parser {

	private static final Logger log = LoggerFactory.getLogger(GcVerboseOpenJdk18Parser.class);

	private static final int BYTES_IN_KB = 1024;
	private static final int MILLIS_IN_SEC = 1000;

	private static final DateTimeFormatter dateTimeFormatter =
			DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withLocale(new Locale("US"));

	// 2017-03-20T15:48:07.506-0100: 1.427: [GC (Allocation Failure) [PSYoungGen: 33280K->4395K(38400K)] 33280K->4403K(125952K), 0.0075649 secs] [Times: user=0.01 sys=0.00, real=0.01 secs]
	private static final Pattern patternJdk8Gc =
			Pattern.compile("(?<timestamp>.*): (.*): \\[GC \\((?<gcType>.*)\\) \\[PSYoungGen: (?<youngBefore>[0-9]*)K->(?<youngAfter>.*)K\\((?<youngTotal>.*)K\\)] (?<heapBefore>.*)K->(?<heapAfter>.*)K\\((?<heapTotal>.*)K\\), (?<gcDurationSec>.*) secs] \\[Times: user=(.*) sys=(.*), real=(.*) secs\\]");
	// 2017-03-20T15:48:10.579-0100: 4.499: [Full GC (Metadata GC Threshold) [PSYoungGen: 5640K->0K(139264K)] [ParOldGen: 8793K->8907K(52736K)] 14434K->8907K(192000K), [Metaspace: 21121K->21121K(1069056K)], 0.0390239 secs] [Times: user=0.12 sys=0.01, real=0.04 secs]
	private static final Pattern patternJdk8FullGc =
			Pattern.compile("(?<timestamp>.*): (.*): \\[Full GC \\((?<gcType>.*)\\) \\[PSYoungGen: (?<youngBefore>[0-9]*)K->(?<youngAfter>.*)K\\((?<youngTotal>.*)K\\)] \\[ParOldGen: (?<oldBefore>.*)K->(?<oldAfter>.*)K\\((?<oldTotal>.*)K\\)] (?<heapBefore>.*)K->(?<heapAfter>.*)K\\((?<heapTotal>.*)K\\), \\[Metaspace: (?<metaBefore>.*)K->(?<metaAfter>.*)K\\((?<metaTotal>.*)K\\)], (?<gcDurationSec>.*) secs] \\[Times: user=(.*) sys=(.*), real=(?<realTime>.*) secs]");

	public List<GcLogEntry> analyse(File gcFile) throws IOException {

		List<GcLogEntry> entries = new ArrayList<>();

		try (BufferedReader gcFileInput = FileUtils.createBufferedReader(gcFile)) {

			String line;
			int gcCount = 0;

			while ((line = gcFileInput.readLine()) != null) {

				boolean foundMatch = false;

				if (line.trim().length() == 0) {
					continue;
				}

				GcLogEntry.GcLogEntryBuilder builder = new GcLogEntry.GcLogEntryBuilder();
				Matcher gcMatcher = patternJdk8Gc.matcher(line);
				if (gcMatcher.find()) {
					foundMatch = true;
					gcCount = gcCount + 1;
					builder.setId(gcCount);
					String gcReason = gcMatcher.group("gcType");
					builder.setGcReason("System.gc()".equals(gcReason) ? GcReason.SYS : GcReason.AF);
					builder.setGcType(GcType.NURSERY);
					fillTimestampAndBytes(builder, gcMatcher);
				}

				if (!foundMatch) {
					Matcher fullGcMatcher = patternJdk8FullGc.matcher(line);
					if (fullGcMatcher.find()) {
						foundMatch = true;
						gcCount = gcCount + 1;
						String gcReason = fullGcMatcher.group("gcType");
						builder.setGcReason("System.gc()".equals(gcReason) ? GcReason.SYS : GcReason.AF);
						builder.setGcType(GcType.GLOBAL);
						builder.setId(gcCount);
						fillTimestampAndBytes(builder, fullGcMatcher);
					}
				}
				if (!foundMatch) {
					log.info("Cannot parse line [{}].", line);
				}
				else {
					GcLogEntry newEntry = builder.createGcLogEntry();
					log.info("Add gc log entry [{}].", newEntry);
					entries.add(newEntry);
				}
			}
		}

		return entries;
	}

	private void fillTimestampAndBytes(GcLogEntry.GcLogEntryBuilder builder, Matcher gcMatcher) {
		DateTime dateTime = dateTimeFormatter.parseDateTime(gcMatcher.group("timestamp"));
		builder.setTimestamp(dateTime.getMillis());
		final double durationMs = Double.parseDouble(gcMatcher.group("gcDurationSec")) * MILLIS_IN_SEC;
		builder.setExclusiveDurationMs(durationMs);
		long nurseryUsedAfterBytes = Long.parseLong(gcMatcher.group("youngAfter")) * BYTES_IN_KB;
		long nurseryTotalBytes = Long.parseLong(gcMatcher.group("youngTotal")) * BYTES_IN_KB;
		long nurseryFreeAfterBytes = nurseryTotalBytes - nurseryUsedAfterBytes;
		builder.setNurseryFreebytes(nurseryFreeAfterBytes);
		builder.setNurseryTotalbytes(nurseryTotalBytes);
		long tenuredUsedAfterBytes = Long.parseLong(gcMatcher.group("heapAfter")) * BYTES_IN_KB;
		long tenuredTotalBytes = Long.parseLong(gcMatcher.group("heapTotal")) * BYTES_IN_KB;
		long tenuredFreeAfterBytes = tenuredTotalBytes - tenuredUsedAfterBytes;
		builder.setTenuredFreebytes(tenuredFreeAfterBytes);
		builder.setTenuredTotalbytes(tenuredTotalBytes);
	}

}

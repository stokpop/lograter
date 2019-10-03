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
package nl.stokpop.lograter.was;

import nl.stokpop.lograter.util.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse WebSphere/OpenJ9 Native Std Error log files to find large allocations.
 */
public class NativeStdErrorParser {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").withLocale(new Locale("US"));

    //JVMDUMP039I Processing dump event "allocation", detail "10273296 bytes, type byte[]" at 2018/08/11 17:10:33 - please wait.
    private static final Pattern allocationStartPattern = Pattern
            .compile("JVMDUMP039I Processing dump event \"allocation\", detail \"([0-9]+) bytes, type (.*)\" at (.*) - please wait.");
    //JVMDUMP013I Processed dump event "allocation", detail "16777232 bytes, type byte[]".
    private static final Pattern allocationEndPattern = Pattern
            .compile("JVMDUMP013I Processed dump event \"allocation\", detail \"([0-9]+) bytes, type (.*)\".");
    //Thread=pool-3-thread-111 (000000000AACFAF0) Status=Running
    private static final Pattern allocationThreadPattern = Pattern
            .compile("Thread=(.*) \\((.*)\\) Status=(.*)");

    public static List<LargeAllocation> getLargeAllocationsFromFile(File nativeStdErrorFile) throws IOException {

        if (!nativeStdErrorFile.exists()) {
            throw new NativeStdErrorParseException("File not found: " + nativeStdErrorFile.getAbsolutePath());
        }

	    return new NativeStdErrorParser().analyse(nativeStdErrorFile);
    }

    public static List<LargeAllocation> getLargeAllocationsFromFiles(List<File> nativeStdErrorFiles) throws IOException {

        List<LargeAllocation> totalAllocations = new ArrayList<>();

        for (File file : nativeStdErrorFiles) {
            final List<LargeAllocation> largeAllocationsFromFile = NativeStdErrorParser.getLargeAllocationsFromFile(file);
            totalAllocations.addAll(largeAllocationsFromFile);
        }

        return totalAllocations;
    }

    private List<LargeAllocation> analyse(File nativeStdErrorFile) throws IOException {

        List<LargeAllocation> allocations = new ArrayList<>();

        BufferedReader fileInput = FileUtils.getBufferedReader(nativeStdErrorFile);

        String line;

        boolean foundAllocationStartEvent = false;
        boolean foundThreadDetails = false;
        boolean foundAllocationEndEvent = false;
        StringBuilder stackTrace = new StringBuilder();
        LargeAllocationBuilder largeAllocationBuilder = new LargeAllocationBuilder();

        while ((line = fileInput.readLine()) != null) {
            if (!foundAllocationStartEvent) {
                Matcher matcher = allocationStartPattern.matcher(line);
                if (matcher.find()) {
                    foundAllocationStartEvent = true;

                    final int bytes = Integer.parseInt(matcher.group(1));
                    final String type = matcher.group(2);
                    final DateTime dateTime = dateTimeFormatter.parseDateTime(matcher.group(3));

                    largeAllocationBuilder.setBytes(bytes);
                    largeAllocationBuilder.setType(type);
                    largeAllocationBuilder.setTimestamp(dateTime);

                    continue;
                }
            }
            if (foundAllocationStartEvent && !foundThreadDetails) {
                Matcher matcher = allocationThreadPattern.matcher(line);
                if (matcher.find()) {
                    foundThreadDetails = true;

                    final String threadName = matcher.group(1);
                    final String threadId = matcher.group(2);
                    final String threadStatus = matcher.group(3);

                    largeAllocationBuilder.setThreadName(threadName);
                    largeAllocationBuilder.setThreadId(threadId);
                    largeAllocationBuilder.setThreadStatus(threadStatus);

                    continue;
                }
            }
            if (foundAllocationStartEvent && foundThreadDetails) {
                Matcher matcher = allocationEndPattern.matcher(line);
                if (matcher.find()) {
                    foundAllocationEndEvent = true;
                    largeAllocationBuilder.setStackTrace(stackTrace.toString());
                }
                else {
                    stackTrace.append(line).append("\n");
                    continue;
                }
            }
            if (foundAllocationEndEvent) {
                allocations.add(largeAllocationBuilder.createLargeAllocation());
                // reset all
                largeAllocationBuilder = new LargeAllocationBuilder();
                stackTrace = new StringBuilder();
                foundAllocationEndEvent = false;
                foundAllocationStartEvent = false;
                foundThreadDetails = false;
            }
        }
        return allocations;
    }
}

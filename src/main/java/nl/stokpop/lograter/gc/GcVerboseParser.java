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
package nl.stokpop.lograter.gc;

import nl.stokpop.lograter.util.time.TimePeriod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses gc verbose files from WebSphere 7 and WebSphere 8.
 */
public class GcVerboseParser {

    private GcVerboseParser() {}

    public static List<GcLogEntry> getGcLogEntriesFromFile(File gcFile, TimePeriod timePeriod) throws IOException {

        if (!gcFile.exists()) {
            throw new GcLogParseException("No file found: " + gcFile.getAbsolutePath());
        }

        JdkGcLogVersion jdkGcLogVersion = JdkGcLogVersion.checkJdkVersion(gcFile);

        List<GcLogEntry> globalGcs;

        if (jdkGcLogVersion == JdkGcLogVersion.WAS7) {
            globalGcs = new GcVerboseWas7Parser().analyse(gcFile);
        }
        else if (jdkGcLogVersion == JdkGcLogVersion.WAS8) {
            globalGcs = new GcVerboseWas8Parser().extractGcLogEntries(gcFile);
        }
        else if (jdkGcLogVersion == JdkGcLogVersion.OPENJDK_1_8) {
        	globalGcs = new GcVerboseOpenJdk18Parser().analyse(gcFile);
        }
        else {
            throw new GcLogParseException("Unknown verbose gc format.");
        }
        List<GcLogEntry> filteredEntries = filterOnTimestamps(globalGcs, timePeriod);
        orderOnTimestamp(filteredEntries);
        return filteredEntries;
    }

    private static List<GcLogEntry> filterOnTimestamps(List<GcLogEntry> globalGcs, TimePeriod timePeriod) {
        List<GcLogEntry> filteredEntries = new ArrayList<>();
        for (GcLogEntry entry : globalGcs) {
            if (timePeriod.isWithinTimePeriod(entry.getTimestamp())) {
                filteredEntries.add(entry);
            }
        }
        return filteredEntries;
    }

    public static void orderOnTimestamp(List<GcLogEntry> globalGcs) {
        globalGcs.sort((o1, o2) -> (int) (o1.getTimestamp() - o2.getTimestamp()));
    }

    public static List<GcLogEntry> getGcLogEntriesFromFiles(List<File> gcFiles, TimePeriod filterPeriod) throws IOException {
        List<GcLogEntry> globalGcs = new ArrayList<>();

        for (File gcFile : gcFiles) {
            globalGcs.addAll(getGcLogEntriesFromFile(gcFile, filterPeriod));
        }
        orderOnTimestamp(globalGcs);
        return globalGcs;
    }

    public static List<GcLogEntry> getGcLogEntriesFromFile(File file) throws IOException {
        return getGcLogEntriesFromFile(file, TimePeriod.createExcludingEndTime(TimePeriod.MIN, TimePeriod.MAX));
    }
}

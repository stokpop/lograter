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

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.util.Calculator;
import nl.stokpop.lograter.util.fit.BestFitLine;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HeapUsageResult {

    private final String description;
    private final List<File> inputFiles;
    private TimePeriod timeWindow;

    Logger log = LoggerFactory.getLogger(HeapUsageResult.class);

    private final List<GcLogEntry> gcLogEntries;

    public HeapUsageResult(String description, List<GcLogEntry> gcLogEntries) {
        this(description, gcLogEntries, Collections.emptyList());
    }

    public HeapUsageResult(String description, List<GcLogEntry> gcLogEntries, List<File> inputFiles) {
        this.gcLogEntries = gcLogEntries;
        this.description = description;
        this.timeWindow = TimePeriod.createExcludingEndTime(getTimeStampFirstEntry(gcLogEntries), getTimeStampLastEntry(gcLogEntries));
        this.inputFiles = inputFiles;
        validateGcLogEntriesOrder(gcLogEntries);
    }

    public HeapUsageResult(List<GcLogEntry> gcLogEntries) {
        this("Unnamed set of gc log entries.", gcLogEntries);
    }

    private static long getTimeStampFirstEntry(List<GcLogEntry> gcLogEntries) {
        int size = gcLogEntries.size();
        if (size == 0) {
            return 0;
        }
        return gcLogEntries.get(0).getTimestamp();
    }

    private static long getTimeStampLastEntry(List<GcLogEntry> gcLogEntries) {
        int size = gcLogEntries.size();
        if (size == 0) {
            return Long.MAX_VALUE;
        }
        return gcLogEntries.get(size - 1).getTimestamp();
    }

    private void validateGcLogEntriesOrder(List<GcLogEntry> gcLogEntries) {
        if (gcLogEntries.size() == 0) return;
        long previousTimestamp = gcLogEntries.get(0).getTimestamp();
        for (int i = 1; i < gcLogEntries.size(); i++) {
            long currentTimestamp = gcLogEntries.get(i).getTimestamp();
            if (previousTimestamp > currentTimestamp) {
                throw new LogRaterException("Gc log entries are out of order: " + previousTimestamp + " is bigger than " + currentTimestamp + " at places " + ( i - 1) + " and " + i + ".");
            }
        }
    }

    public long getAvgTotalHeapUsageInBytes(TimePeriod timeWindow) {
        if (gcLogEntries.size() == 0) return 0;

        long totalHeapSizeInBytes = 0;
        int entries = 0;
        for (GcLogEntry entry : gcLogEntries) {
            if (timeWindow.isWithinTimePeriod(entry.getTimestamp())) {
                totalHeapSizeInBytes += entry.getTotalUsedBytes();
                entries++;
            }
        }
        if (entries == 0) return 0;
        return totalHeapSizeInBytes / entries;
    }

    public long getAvgTotalHeapUsageInBytes() {
        return getAvgTotalHeapUsageInBytes(TimePeriod.MAX_TIME_PERIOD);
    }

    public long getAvgTotalHeapUsageInBytesStandardDev() {
        if (gcLogEntries.size() == 0) return 0;

        long[] totalUsedHeapSizes = new long[gcLogEntries.size()];
        int i = 0;
        for (GcLogEntry entry : gcLogEntries) {
            totalUsedHeapSizes[i++] = entry.getTotalUsedBytes();
        }

        return (long) Calculator.sd(totalUsedHeapSizes);
    }

    public long getAvgTotalHeapUsageTenuredInBytes() {
        if (gcLogEntries.size() == 0) return 0;

        long totalHeapSizeInBytes = 0;
        for (GcLogEntry entry : gcLogEntries) {
            totalHeapSizeInBytes += entry.getTenuredUsedBytes();
        }
        return totalHeapSizeInBytes / gcLogEntries.size();
    }

    public long getAvgTotalHeapUsageTenuredInBytesStandardDev() {
        if (gcLogEntries.size() == 0) return 0;

        long[] totalUsedHeapSizes = new long[gcLogEntries.size()];
        int i = 0;
        for (GcLogEntry entry : gcLogEntries) {
            totalUsedHeapSizes[i++] = entry.getTenuredUsedBytes();
        }

        return (long) Calculator.sd(totalUsedHeapSizes);
    }

    public double calculateGcOverheadPercentage() {
        return calculateGcOverheadPercentage(TimePeriod.MAX_TIME_PERIOD);
    }

        /**
         * Gc overhead is the total time divided by total exclusive gc time.
         *
         * Will crop the actual time to the first found gc entry and the last
         * found gc entry in all gc entries within the time boundary given.
         *
         * @return percentage of gc overhead
         */
    public double calculateGcOverheadPercentage(TimePeriod timeWindow) {

        double exclusiveTimeInMillis = 0;
        long realEndTime = Long.MIN_VALUE;
        long realStartTime = Long.MAX_VALUE;

        for (GcLogEntry entry : gcLogEntries) {
            long timestampInMillis = entry.getTimestamp();
            if (timeWindow.isWithinTimePeriod(timestampInMillis)) {
                   if (timestampInMillis < realStartTime) {
                       realStartTime = timestampInMillis;
                   }
                   if (timestampInMillis > realEndTime) {
                       realEndTime = timestampInMillis;
                   }
                   exclusiveTimeInMillis = exclusiveTimeInMillis + entry.getExclusiveDurationMs();
            }
        }

        long totalTimeInMillis = realEndTime - realStartTime;
        double overhead = exclusiveTimeInMillis/totalTimeInMillis;
        return overhead * 100;
    }

    public double calculateConcurrentGcSuccessRate() {
        return calculateConcurrentGcSuccessRate(TimePeriod.MAX_TIME_PERIOD);
    }

    /**
     * Concurrent-gc success % = ConcurCount / (TotGlob - SystemGCs) * 100%
     * < 95% dan < 90% dan 
     */
    public double calculateConcurrentGcSuccessRate(TimePeriod timeWindow) {

        long globalGcs = countGlobalGcs(timeWindow);
        if (globalGcs == 0) {
            log.warn("No global gcs found for time window: " + timeWindow);
            return 0d;
        }

        long concurrentGcs = countConcurrentGcs(timeWindow);
        long systemGcs = countSysGcs(timeWindow);

        long totalNonSystemGcs = globalGcs - systemGcs;

        return (concurrentGcs/(double) totalNonSystemGcs) * 100d;
    }

    public int countConcurrentGcs() {
        return countConcurrentGcs(TimePeriod.MAX_TIME_PERIOD);
    }

    public int countConcurrentGcs(TimePeriod timeWindow) {
        int countConcurrentGcs = 0;

        for (GcLogEntry entry : gcLogEntries) {
            long timestamp = entry.getTimestamp();
            if (timeWindow.isWithinTimePeriod(timestamp)) {
                if (entry.getGcReason() == GcReason.CON) {
                    countConcurrentGcs++;
                }
            }
        }
        return countConcurrentGcs;
    }

    public int countGlobalGcs() {
        return countGlobalGcs(TimePeriod.MAX_TIME_PERIOD);
    }

    public int countGlobalGcs(TimePeriod timeWindow) {
        List<GcLogEntry> globalGcs = getGlobalGcs(timeWindow);
        return globalGcs.size();
    }

    public List<GcLogEntry> getGlobalGcs() {
        return getGlobalGcs(timeWindow);
    }

    public List<GcLogEntry> getGlobalGcs(TimePeriod timeWindow) {
        List<GcLogEntry> globalGcs = new ArrayList<>();
        for (GcLogEntry entry : gcLogEntries) {
            long timestamp = entry.getTimestamp();
            if (timeWindow.isWithinTimePeriod(timestamp)) {
                if (entry.getType() == GcType.GLOBAL) {
                    globalGcs.add(entry);
                }
            }
        }
        return globalGcs;
    }

    public int countSysGcs() {
        return countSysGcs(TimePeriod.MAX_TIME_PERIOD);
    }

    public int countSysGcs(TimePeriod timeWindow) {
        int countSysGcs = 0;

        for (GcLogEntry entry : gcLogEntries) {
            long timestamp = entry.getTimestamp();
            if (timeWindow.isWithinTimePeriod(timestamp)) {
                if (entry.getGcReason() == GcReason.SYS) {
                    countSysGcs++;
                }
            }
        }
        return countSysGcs;
    }

    public int countNurseryGc() {
        return countNurseryGc(TimePeriod.MAX_TIME_PERIOD);
    }

    public int countNurseryGc(TimePeriod timeWindow) {
        int countNurseryGcs = 0;

        for (GcLogEntry entry : gcLogEntries) {
            long timestamp = entry.getTimestamp();
            if (timeWindow.isWithinTimePeriod(timestamp)) {
                if (entry.getType() == GcType.NURSERY) {
                    countNurseryGcs++;
                }
            }
        }
        return countNurseryGcs;
    }

    public String getDescription() {
        return description;
    }

    /**
     * There should be at least 2 full gcs in the given period,
     * otherwise a LogFileRaterException is thrown.
     */
    public static BestFitLine getBestFitLine(List<GcLogEntry> entriesInFit) throws LogRaterException {

        int sliceSize = entriesInFit.size();

        if (sliceSize < 2) {
            throw new LogRaterException("Cannot determine heap growth per hour on less than 2 global gc entries");
        }
        double[] xValues = new double[sliceSize];
        double[] yValues = new double[sliceSize];

        for (int i = 0; i < sliceSize; i++) {
            GcLogEntry entry = entriesInFit.get(i);
            xValues[i] = entry.getTimestamp();
            yValues[i] = entry.getTenuredUsedBytes();
        }

        return new BestFitLine(xValues, yValues);
    }

    public List<GcLogEntry> getNonSysGsWithDurationLongerThan(int durationInMillis, TimePeriod timeWindow) {
        List<GcLogEntry> longGcEntries = new ArrayList<>();
        for (GcLogEntry entry : gcLogEntries) {
            if (entry.getExclusiveDurationMs() >= durationInMillis && timeWindow.isWithinTimePeriod(entry.getTimestamp()) && entry.getGcReason() != GcReason.SYS) {
                longGcEntries.add(entry);
            }
        }
        return longGcEntries;
    }

    public List<GcLogEntry> getGsWithDurationLongerThan(int durationInMillis, TimePeriod timeWindow) {
        List<GcLogEntry> longGcEntries = new ArrayList<>();
        for (GcLogEntry entry : gcLogEntries) {
            if (entry.getExclusiveDurationMs() >= durationInMillis && timeWindow.isWithinTimePeriod(entry.getTimestamp())) {
                longGcEntries.add(entry);
            }
        }
        return longGcEntries;
    }

    public List<GcLogEntry> getGsWithDurationLongerThan(int durationInMillis) {
        return getGsWithDurationLongerThan(durationInMillis, TimePeriod.MAX_TIME_PERIOD);
    }

    public List<GcLogEntry> getSystemGcs(TimePeriod timeWindow) {
        List<GcLogEntry> longGcEntries = new ArrayList<>();
        for (GcLogEntry entry : gcLogEntries) {
            if (entry.getGcReason() == GcReason.SYS && timeWindow.isWithinTimePeriod(entry.getTimestamp())) {
                longGcEntries.add(entry);
            }
        }
        return longGcEntries;
    }

    public List<GcLogEntry> getNonSystemGlobalGcs(TimePeriod timeWindow) {
        List<GcLogEntry> nonSysGcs = new ArrayList<>();
        for (GcLogEntry entry : gcLogEntries) {
            if (entry.getGcReason() != GcReason.SYS && entry.getType() == GcType.GLOBAL && timeWindow.isWithinTimePeriod(entry.getTimestamp())) {
                nonSysGcs.add(entry);
            }
        }
        return nonSysGcs;
    }

    public List<GcLogEntry> getNonSystemGcs(TimePeriod timeWindow) {
        List<GcLogEntry> nonSysGcs = new ArrayList<>();
        for (GcLogEntry entry : gcLogEntries) {
            if (entry.getGcReason() != GcReason.SYS && timeWindow.isWithinTimePeriod(entry.getTimestamp())) {
                nonSysGcs.add(entry);
            }
        }
        return nonSysGcs;
    }

    public List<File> getInputFiles() {
        return inputFiles;
    }

    public static double findLongestGcMillis(List<GcLogEntry> logEntries) {
        double longestGc = 0;
        for (GcLogEntry entry : logEntries) {
            if (entry.getExclusiveDurationMs() > longestGc) {
                longestGc = entry.getExclusiveDurationMs();
            }
        }
        return longestGc;
    }

    public List<GcLogEntry> getNonSysGsWithDurationLongerThan(int durationInMillis) {
        return getNonSysGsWithDurationLongerThan(durationInMillis, TimePeriod.MAX_TIME_PERIOD);
    }

    public List<GcLogEntry> getSystemGcs() {
        return getSystemGcs(TimePeriod.MAX_TIME_PERIOD);
    }

    public TimePeriod getTimePeriod() {
        return timeWindow;
    }

}

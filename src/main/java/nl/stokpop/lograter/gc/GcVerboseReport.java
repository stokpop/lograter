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
package nl.stokpop.lograter.gc;

import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.fit.BestFitLine;
import nl.stokpop.lograter.util.metric.Point;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GcVerboseReport {

    private static final int MB = 1024 * 1024;
    private static final long HOUR_MILLIS = 1000 * 60 * 60;

    public static final char NL = '\n';

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static final ThreadLocal<NumberFormat> NUMBER_FORMAT_0_DIGITS = ThreadLocal.withInitial(() -> createNumberFormatInstance(0));
    public static final ThreadLocal<NumberFormat> NUMBER_FORMAT = ThreadLocal.withInitial(() -> createNumberFormatInstance(2));
    public static final ThreadLocal<NumberFormat> NUMBER_FORMAT_3_DIGITS = ThreadLocal.withInitial(() -> createNumberFormatInstance(3));
    public static final int MIN_NUMBER_OF_GCS_FOR_FIT = 5;

    private final String logFileRaterVersion;

    public GcVerboseReport() {
        this("Unknown");
    }

    public GcVerboseReport(String logFileRaterVersion) {
        this.logFileRaterVersion = logFileRaterVersion;
    }

    private static NumberFormat createNumberFormatInstance(int fractionDigits) {
        NumberFormat numberInstance = NumberFormat.getNumberInstance(Locale.US);
        numberInstance.setMaximumFractionDigits(fractionDigits);
        numberInstance.setMinimumFractionDigits(fractionDigits);
        numberInstance.setRoundingMode(RoundingMode.HALF_EVEN);
        return numberInstance;
    }

    /**
     * Supply the period for which the fit analysis needs to be performed.
     */
    public void printReport(OutputStream out, HeapUsageResult heapUsageResult, TimePeriod analysisPeriod, TimePeriod memoryAnalysisPeriod, String runId) {
        try (PrintWriter writer = FileUtils.createBufferedPrintWriterWithUTF8(out)) {
            writer.println(generateReportAsString(heapUsageResult, analysisPeriod, memoryAnalysisPeriod, runId));
            writer.flush();
        }
    }

    /**
     * Supply the period for which the fit analysis needs to be performed.
     */
    public String generateReportAsString(HeapUsageResult heapUsageResult, TimePeriod analysisPeriod, TimePeriod memoryAnalysisPeriod, String runId) {
        StringBuilder report = new StringBuilder();

        report.append("Verbose GC Analysis report").append(NL);
        report.append(String.format("LogFileRater version: %s", logFileRaterVersion)).append(NL);
        report.append(String.format("Test run ID: %s", runId)).append(NL).append(NL);


        List<GcLogEntry> gcsForFit = heapUsageResult.getGlobalGcs(memoryAnalysisPeriod);
        double percentageChange = Double.NaN;
        double heapUsage = Double.NaN;
        double qualityOfFit = Double.NaN;
        int numberOfGcs = gcsForFit.size();
        List<Point> outliers = new ArrayList<>();

        if (numberOfGcs > 2) {

            BestFitLine ultimateBestFitLine = HeapUsageResult.getBestFitLine(gcsForFit).createUltimateBestFitLineWithoutOutliers();
            outliers.addAll(ultimateBestFitLine.getRemovedOutliers());

            BestFitLine fitFunctionRelativeX = ultimateBestFitLine.createFitFunctionRelativeX();
            percentageChange = fitFunctionRelativeX.getPercentageChange(0, HOUR_MILLIS);
            heapUsage = fitFunctionRelativeX.getYintersection();
            qualityOfFit = ultimateBestFitLine.getQualityOfFit();
            numberOfGcs = ultimateBestFitLine.getNumberOfDataPoints();
        }

        report.append(createSummary(heapUsageResult, percentageChange, qualityOfFit, analysisPeriod));
        report.append(NL);

        report.append(createSummaryAnalysisPeriod(heapUsageResult, analysisPeriod));
        report.append(NL);
        report.append(createLongGcDurationList(heapUsageResult, analysisPeriod));
        report.append(NL);
        report.append(createSystemGcList(heapUsageResult, analysisPeriod));
        report.append(NL);
        report.append(createMemoryFitAnalysis(numberOfGcs, outliers, percentageChange, heapUsage, qualityOfFit, memoryAnalysisPeriod));
        report.append(NL);

        report.append(createGcSummaryTotal(heapUsageResult));
        report.append(NL);
        report.append(createLongGcDurationList(heapUsageResult));
        report.append(NL);
        report.append(createSystemGcList(heapUsageResult));
        report.append(NL);

        boolean reportGlobalGcs = false;
        if (reportGlobalGcs) {
            report.append(NL);
            report.append(createGlobalGcList(heapUsageResult, analysisPeriod));
        }

        if (heapUsageResult.getInputFiles().size() != 0) {
            report.append(createFileList(heapUsageResult));
            report.append(NL);
        }

        return report.toString();
    }

    private String createSummary(HeapUsageResult heapUsageResult, double percentageChange, double qualityOfFit, TimePeriod timePeriod) {
        StringBuilder summary = new StringBuilder();

        List<GcLogEntry> nonSystemGcs = heapUsageResult.getNonSystemGcs(timePeriod);

        summary.append("== Summary ==");
        summary.append(NL);
        summary.append(String.format(Locale.US, "%-40s %s", "Memory fit period start:", timePeriod.getHumanReadableStartTimestamp())).append(NL);
        summary.append(String.format(Locale.US, "%-40s %s", "Memory fit period end:", timePeriod.getHumanReadableEndTimestamp())).append(NL);
        summary.append(String.format(Locale.US, "%-40s %s", "Memory fit period duration:", timePeriod.getHumanReadableDuration()));
        summary.append(NL);
        summary.append(String.format(Locale.US, "%-40s %-5.2f %%", "Growth per hour:", percentageChange));
        summary.append(NL);
        summary.append(String.format(Locale.US, "%-40s %-5.3f", "Quality of fit:", qualityOfFit));
        summary.append(NL);
        summary.append(String.format(Locale.US, "%-40s %-5.2f %%", "GC overhead:", heapUsageResult.calculateGcOverheadPercentage(timePeriod)));
        summary.append(NL);
        summary.append(String.format(Locale.US, "%-40s %-5d", "System GC count:", heapUsageResult.countSysGcs(timePeriod)));
        summary.append(NL);
        summary.append(String.format(Locale.US, "%-40s %-5d", "Non-forced GC count > 1 sec:", heapUsageResult.getNonSysGsWithDurationLongerThan(1000, timePeriod).size()));
        summary.append(NL);
        summary.append(String.format(Locale.US, "%-40s %-5.0f ms", "Max duration of non-forced GC:", HeapUsageResult.findLongestGcMillis(nonSystemGcs)));
        summary.append(NL);
        summary.append(String.format(Locale.US, "%-40s %-5.0f %%", "Concurrent-GC success rate:", heapUsageResult.calculateConcurrentGcSuccessRate(timePeriod)));
        summary.append(NL);

        return summary.toString();
    }

    private String createFileList(HeapUsageResult heapUsageResult) {
        List<File> inputFiles = heapUsageResult.getInputFiles();
        StringBuilder subReport = new StringBuilder();
        subReport.append("Used input files: ");
        int countFiles = inputFiles.size();
        for (int i = 0; i < countFiles; i++) {

            subReport.append(inputFiles.get(i).getName());
            if (i != countFiles - 1) {
                subReport.append(", ");
            }
        }
        subReport.append(NL);
        return subReport.toString();
    }

    private String createGlobalGcList(HeapUsageResult heapUsageResult, TimePeriod timePeriod) {
        List<GcLogEntry> nonSystemGcs = heapUsageResult.getNonSystemGlobalGcs(timePeriod);
        StringBuilder subReport = new StringBuilder();
        subReport.append("-- All global GCs (non-sys) --").append(NL);
        if (nonSystemGcs.isEmpty()) {
            subReport.append("No global (non-sys) GCs found.").append(NL);
        }
        else {
            subReport.append(formatGcEntryHeader()).append(NL);
            for (GcLogEntry entry : nonSystemGcs) {
                subReport.append(formatGcEntry(entry)).append(NL);
            }
        }
        return subReport.toString();
    }

    private String createSystemGcList(HeapUsageResult heapUsageResult) {
        return createSystemGcList(heapUsageResult, heapUsageResult.getTimePeriod());
    }

    private String createSystemGcList(HeapUsageResult heapUsageResult, TimePeriod timePeriod) {
        List<GcLogEntry> systemGcs = heapUsageResult.getSystemGcs(timePeriod);
        StringBuilder subReport = new StringBuilder();
        subReport.append("-- Sys GCs --").append(NL);
        if (systemGcs.isEmpty()) {
            subReport.append("No sys GCs found.").append(NL);
        }
        else {
            subReport.append(formatGcEntryHeader()).append(NL);
            for (GcLogEntry entry : systemGcs) {
                subReport.append(formatGcEntry(entry)).append(NL);
            }
        }
        return subReport.toString();
    }

    private String formatGcEntryHeader() {
        return String.format(Locale.US, "%-24s %11s %8s %9s %9s %15s", "Timestamp", "Duration ms", "GC Type", "Gc Reason", "Sys Reason", "Used tenured MB");
    }

    private String formatGcEntry(GcLogEntry entry) {
        String date = DATE_TIME_FORMATTER.print(entry.getTimestamp());
        String sysGcReason = entry.getSysGcReason() == null ? "" : entry.getSysGcReason();
        return String.format(Locale.US, "%-24s %11.0f %8s %9s %9s %15.0f", date, entry.getExclusiveDurationMs(), entry.getType(), entry.getGcReason(), sysGcReason, (double) entry.getTenuredUsedBytes() / MB);
    }

    private String createLongGcDurationList(HeapUsageResult heapUsageResult) {
        return createLongGcDurationList(heapUsageResult, heapUsageResult.getTimePeriod());
    }

    private String createLongGcDurationList(HeapUsageResult heapUsageResult, TimePeriod timePeriod) {
        int maxDurationMs = 1000;
        List<GcLogEntry> longGcs = heapUsageResult.getNonSysGsWithDurationLongerThan(maxDurationMs, timePeriod);

        StringBuilder subReport = new StringBuilder();
        subReport.append(String.format("-- GCs longer than %d ms --", maxDurationMs)).append(NL);

        if (longGcs.isEmpty()) {
            List<GcLogEntry> nonSystemGcs = heapUsageResult.getNonSystemGcs(timePeriod);
            double longestGc = HeapUsageResult.findLongestGcMillis(nonSystemGcs);
            subReport.append(String.format("No long GCs found. All are below %.0f ms.", longestGc)).append(NL);
        }
        else {
            double longestGc = HeapUsageResult.findLongestGcMillis(longGcs);
            subReport.append(String.format("There are %d GCs longer than %d ms. All are below %.0f ms.", longGcs.size(), maxDurationMs, longestGc)).append(NL);
            for (GcLogEntry entry : longGcs) {
                subReport.append(formatGcEntry(entry)).append(NL);
            }
        }
        return subReport.toString();
    }

    public String createMemoryFitAnalysis(int nrOfGcs, List<Point> outliers, double percentageChange, double heapUsage, double qualityOfFit, TimePeriod memoryAnalysisTimePeriod) {
        StringBuilder subReport = new StringBuilder();

        subReport.append("== ").append("Used tenured memory fit").append(" ==").append(NL);
        subReport.append(String.format(Locale.US, "%-40s %s", "Memory fit period start:", memoryAnalysisTimePeriod.getHumanReadableStartTimestamp())).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %s", "Memory fit period end:", memoryAnalysisTimePeriod.getHumanReadableEndTimestamp())).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %s", "Memory fit period duration:", memoryAnalysisTimePeriod.getHumanReadableDuration())).append(NL);

        String outliersAsString = formatOutliers(outliers);
        subReport.append(String.format(Locale.US, "%-40s %s", "Outliers:", outliersAsString)).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %-5d", "Gc count:", nrOfGcs)).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %-5.2f %%", "Growth per hour:", percentageChange)).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %-5.0f MB", "Tenured heap usage:", heapUsage / MB)).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %-5.3f (Lower is better, less then 0.025 is considered a good fit.)", "Quality of Fit:", qualityOfFit)).append(NL);

        return subReport.toString();
    }

    public static String formatOutliers(List<Point> outliers) {
        StringBuilder outliersAsString = new StringBuilder();
        if (outliers.isEmpty()) {
            outliersAsString.append("None.");
        } else {
            for (Point outlier : outliers) {
                String timestamp = DATE_TIME_FORMATTER.print((long) outlier.getX());
                String sizeInMB = NUMBER_FORMAT_0_DIGITS.get().format(outlier.getY() / MB);
                outliersAsString.append("(").append(timestamp).append(", ").append(sizeInMB).append(" MB").append(") ");
            }
        }
        return outliersAsString.toString();
    }

    public String createSummaryAnalysisPeriod(HeapUsageResult heapUsageResult, TimePeriod timePeriod) {
        int fitCountGlobalGcs = heapUsageResult.countGlobalGcs(timePeriod);
        int fitCountConcurrentGcs = heapUsageResult.countConcurrentGcs(timePeriod);
        int fitCountNurseryGc = heapUsageResult.countNurseryGc(timePeriod);
        int fitCountSysGcs = heapUsageResult.countSysGcs(timePeriod);
        long fitAvgTotalHeapUsageInBytes = heapUsageResult.getAvgTotalHeapUsageInBytes(timePeriod);
        double fitGcOverheadPercentage = heapUsageResult.calculateGcOverheadPercentage(timePeriod);
        double fitConcurrentGcSuccessRate = heapUsageResult.calculateConcurrentGcSuccessRate(timePeriod);
        String fitSubSectionName = "Analysis Period";
        String fitDescription = heapUsageResult.getDescription();

        return createVerboseGcSummary(fitCountGlobalGcs, fitCountConcurrentGcs, fitCountNurseryGc, fitCountSysGcs, fitGcOverheadPercentage, fitConcurrentGcSuccessRate, fitAvgTotalHeapUsageInBytes, fitSubSectionName, timePeriod, fitDescription);
    }

    public String createGcSummaryTotal(HeapUsageResult heapUsageResult) {
        int countGlobalGcs = heapUsageResult.countGlobalGcs();
        int countConcurrentGcs = heapUsageResult.countConcurrentGcs();
        int countNurseryGc = heapUsageResult.countNurseryGc();
        int countSysGcs = heapUsageResult.countSysGcs();
        double gcOverheadPercentage = heapUsageResult.calculateGcOverheadPercentage();
        double concurrentGcSuccessRate = heapUsageResult.calculateConcurrentGcSuccessRate();
        long avgTotalHeapUsageInBytes = heapUsageResult.getAvgTotalHeapUsageInBytes();

        String subSectionName = "Total Period";

        TimePeriod totalTimePeriod = heapUsageResult.getTimePeriod();

        String description = heapUsageResult.getDescription();

        return createVerboseGcSummary(countGlobalGcs, countConcurrentGcs, countNurseryGc, countSysGcs, gcOverheadPercentage, concurrentGcSuccessRate, avgTotalHeapUsageInBytes, subSectionName, totalTimePeriod, description);
    }

    public String createVerboseGcSummary(int countGlobalGcs, int countConcurrentGcs, int countNurseryGc, int countSysGcs, double gcOverheadPercentage, double concurrentGcSuccessRate, long avgTotalHeapUsageInBytes, String subSectionName, TimePeriod timePeriod, String description) {
        StringBuilder subReport = new StringBuilder();

        subReport.append("== ").append(subSectionName).append(" ==").append(NL);
        subReport.append(String.format(Locale.US, "%-40s %s", "GcLog first entry:", timePeriod.getHumanReadableStartTimestamp())).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %s", "GcLog last entry:", timePeriod.getHumanReadableEndTimestamp())).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %s", "GcLog duration:", timePeriod.getHumanReadableDuration())).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %-5d MB", "Avg used heap after GC:", avgTotalHeapUsageInBytes / MB)).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %-5d", "Global GC count:", countGlobalGcs)).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %-5d", "Concurrent GC count:", countConcurrentGcs)).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %-5d", "Nursery GC count:" ,countNurseryGc)).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %-5d", "System GC count:" ,countSysGcs)).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %-5.2f %%", "Gc overhead:" ,gcOverheadPercentage)).append(NL);
        subReport.append(String.format(Locale.US, "%-40s %-5.2f %% (%d of %d non sys global GCs are concurrent.)", "Concurrent success rate:" , concurrentGcSuccessRate, countConcurrentGcs, countGlobalGcs - countSysGcs)).append(NL);

        return subReport.toString();
    }
}

/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.reportcreator;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.command.CommandGcVerboseLog;
import nl.stokpop.lograter.command.CommandMain;
import nl.stokpop.lograter.gc.*;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Creates report for gc verbose log files from IBM JVMs.
 */
public class GcVerboseLogReportCreator implements ReportCreatorWithCommand<CommandGcVerboseLog> {

	private static final Logger log = LoggerFactory.getLogger(GcVerboseLogReportCreator.class);

	@Override
	public void createReport(PrintWriter outputStream, CommandMain cmdMain, CommandGcVerboseLog cmdGcVerboseLog) throws IOException {
		GcVerboseConfig config = new GcVerboseConfig();
		config.setRunId(cmdMain.runId);
		config.setFilterPeriod(DateUtils.createFilterPeriod(cmdMain.startTimeStr, cmdMain.endTimeStr));
		setAnalysisPeriod(cmdGcVerboseLog, config);

		List<String> filenames = cmdGcVerboseLog.files;
		if (filenames == null || filenames.size() == 0) {
			throw new LogRaterException("Please supply one or more gc verbose log files.");
		}

		List<File> files = FileUtils.findFilesThatMatchFilenames(filenames);
		List<GcLogEntry> gcLogEntriesFromFiles = GcVerboseParser.getGcLogEntriesFromFiles(files, config.getFilterPeriod());

		File reportFile = FileUtils.createFullOutputReportPath(cmdMain.reportDirectory, cmdGcVerboseLog.reportFile);

		log.info("Writing to report file: {}", reportFile.getPath());


		try (OutputStream csvOutputStream = new BufferedOutputStream(new FileOutputStream(reportFile))) {
			String description = "Results for " + Arrays.toString(cmdGcVerboseLog.files.toArray());
			HeapUsageResult heapUsageResult = new HeapUsageResult(description, gcLogEntriesFromFiles, files);

			GcVerboseReport report = new GcVerboseReport(cmdMain.getLogFileRaterVersion());

			long startTimeAnalysis = config.getAnalysisStartTime();
			long endTimeAnalysis =  config.getAnalysisEndTime();
			long startMemoryFit = config.getMemoryFitStartTime();
			long endMemoryFit =  config.getMemoryFitEndTime();

			if (!config.isAnalysisTimePeriodSet()) {
				startTimeAnalysis = heapUsageResult.getTimePeriod().getStartTime() + 60 * 1000 * 60;
				endTimeAnalysis = startTimeAnalysis + 60 * 1000 * 240;
				log.warn("No analysis period given on command line, using 1 hours after first gc entry to 4 hours after first gc entry.");
			}

			if (!config.isMemoryFitPeriodSet()) {
				startMemoryFit = startTimeAnalysis;
				endMemoryFit = endTimeAnalysis;
				log.warn("No memory fit period given on command line, using same times as analysis period.");
			}

			TimePeriod analysisPeriod = TimePeriod.createExcludingEndTime(startTimeAnalysis, endTimeAnalysis);
			TimePeriod memoryAnalysisPeriod = TimePeriod.createExcludingEndTime(startMemoryFit, endMemoryFit);

			report.printReport(csvOutputStream, heapUsageResult, analysisPeriod, memoryAnalysisPeriod, config.getRunId());

		}

		log.info("Check out gc verbose report in file: {}", reportFile.getPath());

	}

	private void setAnalysisPeriod(CommandGcVerboseLog cmdGcVerboseLog, GcVerboseConfig config) {
		config.setAnalysisStartTime(parseDateTime(cmdGcVerboseLog.startTimeAnalysisStr, config.getAnalysisStartTime()));
		config.setAnalysisEndTime(parseDateTime(cmdGcVerboseLog.endTimeAnalysisStr, config.getAnalysisEndTime()));
		config.setMemoryFitStartTime(parseDateTime(cmdGcVerboseLog.startTimeMemoryFitStr, config.getMemoryFitStartTime()));
		config.setMemoryFitEndTime(parseDateTime(cmdGcVerboseLog.endTimeMemoryFitStr, config.getMemoryFitEndTime()));
	}

	private static long parseDateTime(String startTimeAnalysisStr, long defaultValue) {
		long parsedStartTimeAnalysis = defaultValue;
		if (startTimeAnalysisStr != null) {
			String startTimeStr = DateUtils.appendSecondsIfNeeded(startTimeAnalysisStr);
			if (DateUtils.isValidDateTimeString(startTimeStr)) {
				parsedStartTimeAnalysis = DateUtils.parseStandardDateTime(startTimeStr);
			} else {
				log.warn("Invalid formatted date: " + startTimeStr + " default to " + defaultValue + "!");
				parsedStartTimeAnalysis = defaultValue;
			}
		}
		return parsedStartTimeAnalysis;
	}


}

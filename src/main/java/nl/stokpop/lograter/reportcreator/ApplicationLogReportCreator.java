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
package nl.stokpop.lograter.reportcreator;

import nl.stokpop.lograter.LogRater;
import nl.stokpop.lograter.command.CommandApplicationLog;
import nl.stokpop.lograter.command.CommandMain;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.feeder.FileFeeder;
import nl.stokpop.lograter.graphs.ChartFile;
import nl.stokpop.lograter.graphs.HtmlErrorsAndWarnsGraphCreator;
import nl.stokpop.lograter.logentry.LogbackLogEntry;
import nl.stokpop.lograter.parser.ApplicationLogParser;
import nl.stokpop.lograter.parser.line.LogbackParser;
import nl.stokpop.lograter.processor.applicationlog.ApplicationLogConfig;
import nl.stokpop.lograter.processor.applicationlog.ApplicationLogData;
import nl.stokpop.lograter.processor.applicationlog.ApplicationLogProcessor;
import nl.stokpop.lograter.report.text.ApplicationLogTextReport;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Create an application log report.
 */
public class ApplicationLogReportCreator implements ReportCreatorWithCommand<CommandApplicationLog> {

    private static final Logger log = LoggerFactory.getLogger(ApplicationLogReportCreator.class);

	@Override
	public void createReport(PrintWriter outputStream, CommandMain cmdMain, CommandApplicationLog cmdApplicationLog) throws IOException {
		String pattern = StringUtils.useDefaultOrGivenValue(
				"%d;%t;%p;%marker;%X{session-id};%X{alevel};%X{atype};%X{aid};%X{customer};%X{service};%X{serviceName};%X{serviceId};%c;%m%n %xEx{short}",
				cmdApplicationLog.logPattern);

		LogbackParser<LogbackLogEntry> lineParser = LogbackParser.createLogbackParser(pattern);

		ApplicationLogConfig config = new ApplicationLogConfig();
		config.setRunId(cmdMain.runId);
		config.setFilterPeriod(DateUtils.createFilterPeriod(cmdMain.startTimeStr, cmdMain.endTimeStr));

		ApplicationLogProcessor processor = new ApplicationLogProcessor(config);

		ApplicationLogParser appLogParser = new ApplicationLogParser(lineParser);
		appLogParser.addProcessor(processor);

		List<File> files = FileUtils.findFilesThatMatchFilenames(cmdApplicationLog.files);
		FileFeeder feeder = new FileFeeder(files, cmdApplicationLog.fileFeederFilterIncludes, cmdApplicationLog.fileFeederFilterExcludes);
		feeder.feed(appLogParser);

		ApplicationLogData data = processor.getData();

		TimePeriod testPeriod = config.getFilterPeriod();
		long bucketPeriodMillis = 60 * 1000;
		RequestCounter errorsOverTime = data.getErrorsOverTime();
		RequestCounter warnsOverTime = data.getWarnsOverTime();
		File reportDirectory = new File(cmdMain.reportDirectory);

		ChartFile chartFile = HtmlErrorsAndWarnsGraphCreator.createErrorAndWarningGraph(reportDirectory, testPeriod, bucketPeriodMillis, errorsOverTime, warnsOverTime);

		log.info("Check out errors chart: " + chartFile.getFile().getAbsolutePath());

		ApplicationLogTextReport report = new ApplicationLogTextReport(data, config);
		String outputFilename = cmdMain.outputFilename;

		TimePeriod analysisPeriod = data.getLogTimePeriod().createFilterTimePeriodIfFilterIsSet(testPeriod);
		LogRater.writeReport(report, outputFilename, reportDirectory, outputStream, analysisPeriod);
	}

}

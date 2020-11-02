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
package nl.stokpop.lograter.reportcreator;

import nl.stokpop.lograter.LogRater;
import nl.stokpop.lograter.command.CommandAccessLogToCsv;
import nl.stokpop.lograter.command.CommandMain;
import nl.stokpop.lograter.feeder.FileFeeder;
import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.logentry.ApacheLogMapperFactory;
import nl.stokpop.lograter.logentry.UrlSplitter;
import nl.stokpop.lograter.parser.AccessLogParser;
import nl.stokpop.lograter.parser.line.ApacheLogFormatParser;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.processor.accesslog.AccessLogConfig;
import nl.stokpop.lograter.processor.accesslog.AccessLogToCsvProcessor;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.SessionIdParser;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import nl.stokpop.lograter.util.linemapper.LineMapperUtils;
import nl.stokpop.lograter.util.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Create a CSV report from an access log.
 */
public class AccessLogToCsvReportCreator implements ReportCreatorWithCommand<CommandAccessLogToCsv> {

	private static final Logger log = LoggerFactory.getLogger(AccessLogToCsvReportCreator.class);

	@Override
	public void createReport(PrintWriter outputStream, CommandMain cmdMain, CommandAccessLogToCsv cmdAccessLog) throws IOException {

		List<LineMapperSection> lineMappers = cmdAccessLog.useSingleMapper ? LineMapperSection.SINGLE_MAPPER : LineMapperUtils.createLineMapper(cmdAccessLog.mapperFile);

		AccessLogConfig config = new AccessLogConfig();
		config.setRunId(cmdMain.runId);
		LogRater.populateBasicCounterLogSettings(cmdAccessLog, config);
		config.setFilterPeriod(DateUtils.createFilterPeriod(cmdMain.startTimeStr, cmdMain.endTimeStr));
		config.setLineMappers(lineMappers);
		config.setFileFeederFilterIncludes(cmdAccessLog.fileFeederFilterIncludes);
		config.setFileFeederFilterExcludes(cmdAccessLog.fileFeederFilterExcludes);
		config.setCounterStorage(cmdMain.storage);
		config.setDetermineClickpaths(cmdAccessLog.determineClickpaths);
		config.setClickpathReportStepDurations(cmdAccessLog.clickpathReportStepDurations);
		config.setDetermineSessionDuration(cmdAccessLog.determineSessionDuration);
		config.setSessionField(cmdAccessLog.sessionField);
		config.setSessionFieldRegexp(cmdAccessLog.sessionFieldRegexp);
		config.setLogPattern(cmdAccessLog.logPattern);

		String reportDirectory = cmdMain.reportDirectory;

        String csvFilename = cmdAccessLog.csvFile;

        File csvFile = FileUtils.createFullOutputReportPath(reportDirectory, csvFilename);

        String pattern = StringUtils.useDefaultOrGivenValue(
				"\"%{X-Client-IP}i\" %V %t \"%r\" %>s %b %D \"%{x-host}i\" \"%{Referer}i\" \"%{User-Agent}i\"",
				config.getLogPattern());

		List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);

		UrlSplitter splitter = config.isRemoveParametersFromUrl() ? AccessLogEntry.URL_SPLITTER_DEFAULT : null;

		Map<String, LogEntryMapper<AccessLogEntry>> mappers =
				ApacheLogMapperFactory.initializeMappers(elements, splitter, config.getBaseUnit());
		ApacheLogFormatParser<AccessLogEntry> lineParser =
				new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);
		
		log.info("Writing to csv file: {}", csvFile.getPath());

		try (OutputStream csvOutputStream = new BufferedOutputStream(new FileOutputStream(csvFile))) {
			// TODO: using default mappers now... or rather the first mapper table only
			AccessLogToCsvProcessor processor = new AccessLogToCsvProcessor(csvOutputStream, lineMappers.get(0),
					new SessionIdParser(config.getSessionField(), config.getSessionFieldRegexp()));

			AccessLogParser parser = new AccessLogParser(lineParser, config.getFilterPeriod());
			parser.addProcessor(processor);

			FileFeeder feeder = new FileFeeder(cmdAccessLog.fileFeederFilterIncludes, cmdAccessLog.fileFeederFilterExcludes);
			feeder.feedFilesAsString(cmdAccessLog.files, parser);

		}

		log.info("Check out result in csv file: {}", csvFile.getPath());

	}

}

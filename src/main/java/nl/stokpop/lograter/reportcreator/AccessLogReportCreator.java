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

import nl.stokpop.lograter.GraphConfig;
import nl.stokpop.lograter.LogRater;
import nl.stokpop.lograter.clickpath.ClickPathReport;
import nl.stokpop.lograter.command.AbstractCommandAccessLog;
import nl.stokpop.lograter.command.CommandAccessLog;
import nl.stokpop.lograter.command.CommandMain;
import nl.stokpop.lograter.feeder.FileFeeder;
import nl.stokpop.lograter.graphs.LogGraphCreator;
import nl.stokpop.lograter.processor.accesslog.AccessLogConfig;
import nl.stokpop.lograter.processor.accesslog.AccessLogDataBundle;
import nl.stokpop.lograter.processor.accesslog.AccessLogReader;
import nl.stokpop.lograter.report.text.AccessLogTextReport;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import nl.stokpop.lograter.util.linemapper.LineMapperUtils;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static nl.stokpop.lograter.logentry.LogEntry.HTTP_METHOD;
import static nl.stokpop.lograter.logentry.LogEntry.HTTP_STATUS;

/**
 * Process access logs.
 */
public class AccessLogReportCreator implements ReportCreatorWithCommand<AbstractCommandAccessLog> {

	private static final Logger log = LoggerFactory.getLogger(AccessLogReportCreator.class);

	@Override
	public void createReport(PrintWriter outputStream, CommandMain cmdMain, AbstractCommandAccessLog cmdAccessLog) throws IOException {
		List<LineMapperSection> lineMappers = cmdAccessLog.useSingleMapper ? LineMapperSection.SINGLE_MAPPER : LineMapperUtils.createLineMapper(cmdAccessLog.mapperFile);

		AccessLogConfig config = new AccessLogConfig();
		config.setRunId(cmdMain.runId);
		config.setFilterPeriod(DateUtils.createFilterPeriod(cmdMain.startTimeStr, cmdMain.endTimeStr));
		config.setDoCountMultipleMapperHits(cmdAccessLog.doCountMultipleMapperHits);
		// insert order is important
		Set<String> groupByFields = new LinkedHashSet<>();
		if(cmdAccessLog.doGroupByHttpMethod) {
			groupByFields.add(HTTP_METHOD);
		}
		if(cmdAccessLog.doGroupByHttpStatus) {
			groupByFields.add(HTTP_STATUS);
		}
		groupByFields.addAll(cmdAccessLog.groupByFields);
		config.setGroupByFields(Collections.unmodifiableList(new ArrayList<>(groupByFields)));
		config.setExcludeMappersInIisAndAccessLogs(cmdAccessLog.excludeMappers);
		config.setIgnoreMultiAndNoMatches(cmdAccessLog.ignoreMultiAndNoMatches);
		config.setCountNoMappersAsOne(cmdAccessLog.countNoMappersAsOne);
		config.setLineMappers(lineMappers);
		config.setShowBasicUrls(cmdAccessLog.showBasicUrls);
		config.setShowReferers(cmdAccessLog.showReferers);
		config.setShowUserAgents(cmdAccessLog.showUserAgents);

		LogRater.populateBasicCounterLogSettings(cmdAccessLog, config);

		config.setClickpathReportStepDurations(cmdAccessLog.clickpathReportStepDurations);
		config.setRemoveParametersFromUrl(cmdAccessLog.removeParametersFromUrl);
		config.setFileFeederFilterIncludes(cmdAccessLog.fileFeederFilterIncludes);
		config.setFileFeederFilterExcludes(cmdAccessLog.fileFeederFilterExcludes);
		config.setCounterStorage(cmdMain.storage);
		config.setCounterStorageDir(cmdMain.storageDir);
		config.setDetermineClickpaths(cmdAccessLog.determineClickpaths);
		config.setDetermineSessionDuration(cmdAccessLog.determineSessionDuration);
		config.setClickpathEndOfSessionSnippet(cmdAccessLog.clickpathEndOfSessionSnippet);
		config.setSessionField(cmdAccessLog.sessionField);
		config.setSessionFieldRegexp(cmdAccessLog.sessionFieldRegexp);
		config.setLogPattern(cmdAccessLog.logPattern);
		config.setIncludeMapperRegexpColumn(cmdAccessLog.includeMapperRegexpColumn);
		if (cmdAccessLog instanceof CommandAccessLog) {
            config.setLogType(((CommandAccessLog) cmdAccessLog).logType);
        }
        config.setBaseUnit(cmdMain.baseUnit);

		List<File> files = FileUtils.findFilesThatMatchFilenames(cmdAccessLog.files);

		AccessLogReader accessLogReader = new AccessLogReader();
		AccessLogDataBundle accessLogDataBundle = accessLogReader.readAndProcessAccessLogs(config, new FileFeeder(files));

		AccessLogTextReport report = new AccessLogTextReport(accessLogDataBundle);

		File reportDirectory = new File(cmdMain.reportDirectory);
		String reportOutputFileName = cmdMain.outputFilename;

		TimePeriod analysisPeriod = accessLogDataBundle.getTotalRequestCounterStorePair().totalTimePeriod().createFilterTimePeriodIfFilterIsSet(config.getFilterPeriod());

		LogRater.writeReport(report, reportOutputFileName, reportDirectory, outputStream, analysisPeriod);

		GraphConfig graphConfig = new GraphConfig();
		graphConfig.setAggregateDurationInSeconds(cmdAccessLog.aggregateDurationInSeconds);
		graphConfig.setGraphsHistoEnabled(cmdAccessLog.graphsHisto);
		graphConfig.setGraphsHistoSimulatorEnabled(cmdAccessLog.graphsHistoSimulator);
		graphConfig.setGraphsPercentileEnabled(cmdAccessLog.graphsPercentile);
		graphConfig.setGraphsResponseTimesEnabled(cmdAccessLog.graphsResponseTimes);
		graphConfig.setGraphsTpsEnabled(cmdAccessLog.graphsTps);
		graphConfig.setGraphsHtmlEnabled(cmdAccessLog.graphsHtml);
		graphConfig.setGraphWithTrueTPSEnabled(cmdAccessLog.graphWithTrueTPS);
		graphConfig.setBaseUnit(cmdMain.baseUnit);

		if (graphConfig.isGraphRequested()) {
			LogGraphCreator graphCreator = new LogGraphCreator(graphConfig);

			File graphFile = graphCreator.createHtmlChartFile(reportDirectory, "access-log-rater-charts.html", accessLogDataBundle.getRequestCounterStorePairs(), analysisPeriod);
			log.info("Check out graphs: {}", graphFile);
		}

		if (accessLogDataBundle.getClickPathCollector() != AccessLogDataBundle.NOOP_CLICK_PATH_COLLECTOR) {
			File clickpathFile = new File(reportDirectory, "clickpath-report-" + System.currentTimeMillis() + ".csv");
			ClickPathReport.reportClickpaths(accessLogDataBundle.getClickPathCollector(), clickpathFile, config.isClickpathReportStepDurations());
			log.info("The clickpath report: {}", clickpathFile.getPath());
		}
	}
}

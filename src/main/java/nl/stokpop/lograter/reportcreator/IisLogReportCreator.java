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
package nl.stokpop.lograter.reportcreator;

import nl.stokpop.lograter.GraphConfig;
import nl.stokpop.lograter.LogRater;
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.clickpath.ClickPathAnalyser;
import nl.stokpop.lograter.clickpath.ClickPathAnalyserEngine;
import nl.stokpop.lograter.clickpath.ClickPathReport;
import nl.stokpop.lograter.clickpath.InMemoryClickpathCollector;
import nl.stokpop.lograter.command.CommandIisLog;
import nl.stokpop.lograter.command.CommandMain;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.feeder.FileFeeder;
import nl.stokpop.lograter.graphs.LogGraphCreator;
import nl.stokpop.lograter.parser.IisLogParser;
import nl.stokpop.lograter.parser.line.IisLogFormatParser;
import nl.stokpop.lograter.processor.accesslog.AccessLogClickPathProcessor;
import nl.stokpop.lograter.processor.accesslog.AccessLogConfig;
import nl.stokpop.lograter.processor.accesslog.AccessLogDataBundle;
import nl.stokpop.lograter.processor.accesslog.AccessLogReader;
import nl.stokpop.lograter.processor.accesslog.AccessLogUrlMapperProcessor;
import nl.stokpop.lograter.processor.accesslog.AccessLogUserSessionProcessor;
import nl.stokpop.lograter.report.text.AccessLogTextReport;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.LogRaterUtils;
import nl.stokpop.lograter.util.SessionIdParser;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.linemapper.LineMap;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import nl.stokpop.lograter.util.linemapper.LineMapperUtils;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.SessionDurationCalculator;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generate Iis report.
 */
public class  IisLogReportCreator implements ReportCreatorWithCommand<CommandIisLog> {

	private static final Logger log = LoggerFactory.getLogger(IisLogReportCreator.class);

	@Override
	public void createReport(PrintStream outputStream, CommandMain cmdMain, CommandIisLog cmdIisLog) throws IOException {
		List<LineMapperSection> lineMappers = LineMapperUtils.createLineMapper(cmdIisLog.mapperFile);

		RequestCounterStoreFactory csFactory = new RequestCounterStoreFactory(cmdMain.storage);

		AccessLogConfig config = new AccessLogConfig();
		config.setRunId(cmdMain.runId);
		config.setFilterPeriod(DateUtils.createFilterPeriod(cmdMain.startTimeStr, cmdMain.endTimeStr));
		config.setDoCountMultipleMapperHits(cmdIisLog.doCountMultipleMapperHits);
		config.setDoFilterOnHttpMethod(cmdIisLog.doGroupByHttpMethod);
		config.setDoFilterOnHttpStatus(cmdIisLog.doGroupByHttpStatus);
		config.setExcludeMappersInIisAndAccessLogs(cmdIisLog.excludeMappers);
		config.setIgnoreMultiAndNoMatches(cmdIisLog.ignoreMultiAndNoMatches);
		config.setLineMappers(lineMappers);
		config.setShowBasicUrls(cmdIisLog.showBasicUrls);
		config.setShowReferers(cmdIisLog.showReferers);
		config.setShowUserAgents(cmdIisLog.showUserAgents);
		config.setIncludeMapperRegexpColumn(cmdIisLog.includeMapperRegexpColumn);
		config.setDetermineClickpaths(cmdIisLog.determineClickpaths);
		config.setClickpathReportStepDurations(cmdIisLog.clickpathReportStepDurations);
		config.setClickpathEndOfSessionSnippet(cmdIisLog.clickpathEndOfSessionSnippet);
		config.setDetermineSessionDuration(cmdIisLog.determineSessionDuration);
		config.setSessionField(cmdIisLog.sessionField);
		config.setSessionFieldRegexp(cmdIisLog.sessionFieldRegexp);
		config.setFailureAwareAnalysis(true);
		config.setCounterStorage(cmdMain.storage);
		config.setCounterStorageDir(cmdMain.storageDir);

		LogRater.populateBasicCounterLogSettings(cmdIisLog, config);

		String pattern = StringUtils.useDefaultOrGivenValue(
				"#Fields: date time cs-method cs-uri-stem cs-uri-query s-port cs-username c-ip cs-version cs(User-Agent) cs(Cookie) cs(Referer) sc-status sc-substatus sc-win32-status sc-bytes cs-bytes time-taken",
				cmdIisLog.logPattern);

        IisLogFormatParser lineParser = IisLogFormatParser.createIisLogFormatParser(pattern);

        List<AccessLogUrlMapperProcessor> urlMapperProcessors = LineMapperUtils.createUrlMapperProcessors(csFactory, config);

		IisLogParser iisLogParser = new IisLogParser(lineParser, new SessionIdParser(config.getSessionField(), config.getSessionFieldRegexp()));

		AccessLogUserSessionProcessor userSessionProcessor = null;
		if (config.isDetermineSessionDurationEnabled()) {
			if (LogRaterUtils.isEmpty(config.getSessionField())) {
				throw new LogRaterException("If user session duration need to be determined, then supply a session field");
			}
			SessionDurationCalculator calculator = new SessionDurationCalculator();
			userSessionProcessor = new AccessLogUserSessionProcessor(calculator);
			iisLogParser.addProcessor(userSessionProcessor);
		}

		AccessLogClickPathProcessor clickPathProcessor = null;
		InMemoryClickpathCollector clickPathCollector = null;

		if (config.isDetermineClickpathsEnabled()) {
			if (LogRaterUtils.isEmpty(config.getSessionField())) {
				throw new LogRaterException("If clickpaths need to be determined, then supply a session field");
			}
			clickPathCollector = new InMemoryClickpathCollector();
			ClickPathAnalyser clickPathAnalyser = new ClickPathAnalyserEngine(clickPathCollector, config.getClickpathEndOfSessionSnippet());
			// TODO: using default mappers now... or rather the first mapper table only
			clickPathProcessor = new AccessLogClickPathProcessor(clickPathAnalyser, config.getLineMappers().get(0));
			iisLogParser.addProcessor(clickPathProcessor);
		}

		// collect the results
		List<RequestCounterStorePair> requestCounterStorePairs = new ArrayList<>();

		for (AccessLogUrlMapperProcessor urlMapperProcessor : urlMapperProcessors) {
			iisLogParser.addProcessor(urlMapperProcessor);
			RequestCounterStore storeSuccess = urlMapperProcessor.getMappersRequestCounterStoreSuccess();
			RequestCounterStore storeFailure = urlMapperProcessor.getMappersRequestCounterStoreFailure();

			RequestCounterStorePair storePair = new RequestCounterStorePair(storeSuccess, storeFailure);
			
			requestCounterStorePairs.add(storePair);
		}

		int additionalColumns = 0;
		if (cmdIisLog.doGroupByHttpMethod) { additionalColumns++; }
		if (cmdIisLog.doGroupByHttpStatus) { additionalColumns++; }
		final String totalCounterName = RequestCounter.createCounterNameThatAlignsInTextReport("TOTAL", additionalColumns);

		RequestCounterStorePair totalRequestCounterStorePair = AccessLogReader.addTotalRequestCounterStoreToLogFileParser(csFactory, iisLogParser, totalCounterName);

		requestCounterStorePairs.addAll(AccessLogReader.createAccessLogCounterProcessors(iisLogParser, config, csFactory));

		FileFeeder feeder = new FileFeeder(cmdIisLog.fileFeederFilterIncludes, cmdIisLog.fileFeederFilterExcludes);
		feeder.feedFilesAsString(cmdIisLog.files, iisLogParser);

		if (clickPathProcessor != null) {
			clickPathProcessor.getClickPathAnalyser().closeAllRemainingSessions();
		}

		if (config.isDetermineSessionDurationEnabled() & userSessionProcessor != null) {
			SessionDurationCalculator calculator = userSessionProcessor.getSessionDurationCalculator();
			long avgSessionDuration = calculator.getAvgSessionDuration();
			TimePeriod avgSessionDurationPeriod = TimePeriod.createExcludingEndTime(0, avgSessionDuration);
			log.info("Avg user session duration: {} ms ({})", avgSessionDuration, avgSessionDurationPeriod.getHumanReadableDuration());
		}
		else {
			log.info("Avg user session duration calculation is disabled.");
		}

		RequestCounter totalRequestCounterSuccess = totalRequestCounterStorePair.getRequestCounterStoreSuccess().getTotalRequestCounter();
		RequestCounter totalRequestCounterFailure = totalRequestCounterStorePair.getStoreFailure().getTotalRequestCounter();
		if (totalRequestCounterSuccess == null && totalRequestCounterFailure == null) {
			throw new LogRaterException("No lines (success or failure) processed in file feeder. Please check input parameters.");
		}

		Map<String, LineMap> allCounterKeysToLineMapMap = new HashMap<>();
		for (AccessLogUrlMapperProcessor urlMapperProcessor : urlMapperProcessors) {
			Map<String, LineMap> counterKeyToLineMapMap = urlMapperProcessor.getCounterKeyToLineMapMap();
			allCounterKeysToLineMapMap.putAll(counterKeyToLineMapMap);
		}
		
		AccessLogDataBundle dataBundle = clickPathCollector == null ?
				new AccessLogDataBundle(config, requestCounterStorePairs, totalRequestCounterStorePair) :
				new AccessLogDataBundle(config, requestCounterStorePairs, totalRequestCounterStorePair, clickPathCollector, allCounterKeysToLineMapMap);

		AccessLogTextReport report = new AccessLogTextReport(dataBundle);

		TimePeriod analysisPeriod = totalRequestCounterStorePair.totalTimePeriod().createFilterTimePeriodIfFilterIsSet(config.getFilterPeriod());

		LogRater.writeReport(report, cmdMain.outputFilename, new File(cmdMain.reportDirectory), outputStream, analysisPeriod);

		File reportDir = new File(cmdMain.reportDirectory);

		GraphConfig graphConfig = new GraphConfig();
		graphConfig.setAggregateDurationInSeconds(cmdIisLog.aggregateDurationInSeconds);
		graphConfig.setGraphsHistoEnabled(cmdIisLog.graphsHisto);
		graphConfig.setGraphsHistoSimulatorEnabled(cmdIisLog.graphsHistoSimulator);
		graphConfig.setGraphsPercentileEnabled(cmdIisLog.graphsPercentile);
		graphConfig.setGraphsResponseTimesEnabled(cmdIisLog.graphsResponseTimes);
		graphConfig.setGraphsTpsEnabled(cmdIisLog.graphsTps);
		graphConfig.setGraphsHtmlEnabled(cmdIisLog.graphsHtml);
		graphConfig.setGraphWithTrueTPSEnabled(cmdIisLog.graphWithTrueTPS);
		graphConfig.setBaseUnit(cmdMain.baseUnit);

		if (graphConfig.isGraphRequested()) {
			LogGraphCreator graphCreator = new LogGraphCreator(graphConfig);
			File graphFile = graphCreator.createHtmlChartFile(reportDir, "iis-log-rater-charts.html", dataBundle.getRequestCounterStorePairs(), analysisPeriod);
			log.info("Check out graphs: {}", graphFile);
		}

		if (dataBundle.getClickPathCollector() != AccessLogDataBundle.NOOP_CLICK_PATH_COLLECTOR) {
			File clickpathFile = new File(reportDir, "clickpath-report-" + System.currentTimeMillis() + ".csv");
			ClickPathReport.reportClickpaths(dataBundle.getClickPathCollector(), clickpathFile, config.isClickpathReportStepDurations());
			log.info("The clickpath report: {}", clickpathFile.getPath());
		}

	}

}

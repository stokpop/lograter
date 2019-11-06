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
package nl.stokpop.lograter.processor.accesslog;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.clickpath.ClickPathAnalyser;
import nl.stokpop.lograter.clickpath.ClickPathAnalyserEngine;
import nl.stokpop.lograter.clickpath.InMemoryClickpathCollector;
import nl.stokpop.lograter.command.CommandAccessLog;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.feeder.FileFeeder;
import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.logentry.ApacheLogMapperFactory;
import nl.stokpop.lograter.logentry.NginxLogMapperFactory;
import nl.stokpop.lograter.logentry.UrlSplitter;
import nl.stokpop.lograter.parser.AccessLogParser;
import nl.stokpop.lograter.parser.LogFileParser;
import nl.stokpop.lograter.parser.line.ApacheLogFormatParser;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.parser.line.NginxLogFormatParser;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.linemapper.LineMap;
import nl.stokpop.lograter.util.linemapper.LineMapperUtils;
import nl.stokpop.lograter.util.time.SessionDurationCalculator;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read access and nginx logs.
 */
public class AccessLogReader {

    private final static Logger log = LoggerFactory.getLogger(AccessLogReader.class);

    public AccessLogDataBundle readAndProcessAccessLogs(AccessLogConfig config, List<File> files) {

        RequestCounterStoreFactory csFactory =
		        new RequestCounterStoreFactory(config.getCounterStorage(), config.getFilterPeriod(), new File(config.getCounterStorageDir()));

        List<AccessLogUrlMapperProcessor> urlMapperProcessors = LineMapperUtils.createUrlMapperProcessors(csFactory, config);

        final AccessLogParser accessLogParser;
        final CommandAccessLog.LogType logType = config.getLogType();

        final UrlSplitter urlSplitter = config.isRemoveParametersFromUrl() ? AccessLogEntry.URL_SPLITTER_DEFAULT : null;

        if (logType == CommandAccessLog.LogType.apache) {
            String pattern = StringUtils.useDefaultOrGivenValue(
                    "\"%{X-Client-IP}i\" %V %t \"%r\" %>s %b %D \"%{x-host}i\" \"%{Referer}i\" \"%{User-Agent}i\"",
                    config.getLogPattern());

            List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);

            Map<String, LogEntryMapper<AccessLogEntry>> mappers =
                    ApacheLogMapperFactory.initializeMappers(elements, urlSplitter, config.getBaseUnit());
            ApacheLogFormatParser<AccessLogEntry> lineParser =
                    new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry.class);

            accessLogParser = new AccessLogParser(lineParser, config.getFilterPeriod());
        }
        else if (logType == CommandAccessLog.LogType.nginx){
            String pattern = StringUtils.useDefaultOrGivenValue(
                    "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\"",
                    config.getLogPattern());

            List<LogbackElement> elements = NginxLogFormatParser.parse(pattern);
            Map<String, LogEntryMapper<AccessLogEntry>> mappers =
                    NginxLogMapperFactory.initializeMappers(elements, urlSplitter);
            NginxLogFormatParser<AccessLogEntry> lineParser =
                    new NginxLogFormatParser<>(elements, mappers, AccessLogEntry.class);

            accessLogParser = new AccessLogParser(lineParser, config.getFilterPeriod());
        }
        else {
            String msg = "Unsupported log type:" + logType;
            log.error(msg);
            throw new LogRaterException(msg);
        }

        List<RequestCounterStorePair> requestCounterStoresPairs = new ArrayList<>();

        for (AccessLogUrlMapperProcessor urlMapperProcessor : urlMapperProcessors) {
            accessLogParser.addProcessor(urlMapperProcessor);
	        RequestCounterStore storeSuccess = urlMapperProcessor.getMappersRequestCounterStoreSuccess();
	        RequestCounterStore storeFailure = urlMapperProcessor.getMappersRequestCounterStoreFailure();
	        RequestCounterStorePair storePair = new RequestCounterStorePair(storeSuccess, storeFailure);
	        requestCounterStoresPairs.add(storePair);
        }

	    AccessLogUserSessionProcessor userSessionProcessor = null;
	    if (config.isDetermineSessionDurationEnabled()) {
		    SessionDurationCalculator calculator = new SessionDurationCalculator();
		    userSessionProcessor = new AccessLogUserSessionProcessor(calculator);
		    accessLogParser.addProcessor(userSessionProcessor);
	    }

        AccessLogClickPathProcessor clickPathProcessor = null;
        InMemoryClickpathCollector clickPathCollector = null;

        if (config.isDetermineClickpathsEnabled()) {
            clickPathCollector = new InMemoryClickpathCollector();
            ClickPathAnalyser clickPathAnalyser = new ClickPathAnalyserEngine(clickPathCollector, config.getClickpathEndOfSessionSnippet());
            // TODO: using default mappers now... or rather the first mapper table only
            clickPathProcessor = new AccessLogClickPathProcessor(clickPathAnalyser, config.getLineMappers().get(0));
            accessLogParser.addProcessor(clickPathProcessor);
        }

        int additionalColumns = 0;
        if (config.groupByHttpMethod()) { additionalColumns++; }
        if (config.groupByHttpStatus()) { additionalColumns++; }
        if (config.getGroupByFields() != null) { additionalColumns = additionalColumns + config.getGroupByFields().size(); }
        final String totalCounterName = RequestCounter.createCounterNameThatAlignsInTextReport("TOTAL", additionalColumns);

        RequestCounterStorePair totalRequestCounterStorePair =
		        addTotalRequestCounterStoreToLogFileParser(csFactory, accessLogParser, totalCounterName);

        requestCounterStoresPairs.addAll(createAccessLogCounterProcessors(accessLogParser, config, csFactory));

        FileFeeder feeder = new FileFeeder(config.getFileFeederFilterIncludes(), config.getFileFeederFilterExcludes());
        feeder.feedFiles(files, accessLogParser);

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

        log.info("Read [{}] successful and [{}] failed access log entries for the following time period: [{}]",
		        totalRequestCounterSuccess.getHits(), totalRequestCounterFailure.getHits(), totalRequestCounterSuccess.getTimePeriod());

        Map<String, LineMap> allCounterKeysToLineMapMap = new HashMap<>();
        for (AccessLogUrlMapperProcessor urlMapperProcessor : urlMapperProcessors) {
            Map<String, LineMap> counterKeyToLineMapMap = urlMapperProcessor.getCounterKeyToLineMapMap();
            allCounterKeysToLineMapMap.putAll(counterKeyToLineMapMap);
        }
	    
	    return new AccessLogDataBundle(config, requestCounterStoresPairs,totalRequestCounterStorePair, clickPathCollector, allCounterKeysToLineMapMap);
    }

    public static List<RequestCounterStorePair> createAccessLogCounterProcessors(
            final LogFileParser<AccessLogEntry> accessLogParser,
            final AccessLogConfig config,
            final RequestCounterStoreFactory csFactory) {

    	List<RequestCounterStorePair> requestCounterStorePairs = new ArrayList<>();

        if (config.isShowBasicUrls()) {
            RequestCounterStore urlCounterStoreSuccess = csFactory.newInstance("url-success");
            RequestCounterStore urlCounterStoreFailure = csFactory.newInstance("url-failure");

            RequestCounterStorePair urlStorePair = new RequestCounterStorePair(urlCounterStoreSuccess, urlCounterStoreFailure);

            AccessLogCounterKeyCreator keyCreator = new AccessLogCounterKeyCreator(config.groupByHttpMethod(), config.groupByHttpStatus(), config.getGroupByFields()) {
                @Override public String counterKeyBaseName(AccessLogEntry entry) { return entry.getUrl(); }
            };
            accessLogParser.addProcessor(new AccessLogCounterProcessor(urlStorePair, keyCreator));
            requestCounterStorePairs.add(urlStorePair);
        }
        if (config.isShowUserAgents()) {
            RequestCounterStore userAgentCounterStoreSuccess = csFactory.newInstance("userAgent-success");
            RequestCounterStore userAgentCounterStoreFailure = csFactory.newInstance("userAgent-failure");

            RequestCounterStorePair userAgentStorePair = new RequestCounterStorePair(userAgentCounterStoreSuccess, userAgentCounterStoreFailure);

            AccessLogCounterKeyCreator keyCreator = new AccessLogCounterKeyCreator(config.groupByHttpMethod(), config.groupByHttpStatus(), config.getGroupByFields()) {
                @Override public String counterKeyBaseName(AccessLogEntry entry) { return entry.getUserAgent(); }
            };
            accessLogParser.addProcessor(new AccessLogCounterProcessor(userAgentStorePair, keyCreator));
            requestCounterStorePairs.add(userAgentStorePair);
        }
        if (config.isShowReferers()) {
            RequestCounterStore referersCounterStoreSuccess = csFactory.newInstance("referer-success");
            RequestCounterStore referersCounterStoreFailure = csFactory.newInstance("referer-failure");

            RequestCounterStorePair referersStorePair = new RequestCounterStorePair(referersCounterStoreSuccess, referersCounterStoreFailure);

            AccessLogCounterKeyCreator keyCreator = new AccessLogCounterKeyCreator(config.groupByHttpMethod(), config.groupByHttpStatus(), config.getGroupByFields()) {
                @Override public String counterKeyBaseName(AccessLogEntry entry) { return entry.getReferrer(); }
            };
            accessLogParser.addProcessor(new AccessLogCounterProcessor(referersStorePair, keyCreator));
            requestCounterStorePairs.add(referersStorePair);
        }
        return requestCounterStorePairs;
    }

    public static RequestCounterStorePair addTotalRequestCounterStoreToLogFileParser(RequestCounterStoreFactory csFactory, LogFileParser<AccessLogEntry> logFileParser, final String totalCounterName) {
        RequestCounterStore totalCounterStoreSuccess = csFactory.newInstance(String.join("-", totalCounterName, "success"));
        RequestCounterStore totalCounterStoreFailure = csFactory.newInstance(String.join("-", totalCounterName, "failure"));

        RequestCounterStorePair totalStorePair = new RequestCounterStorePair(totalCounterStoreSuccess, totalCounterStoreFailure);

        AccessLogCounterKeyCreator overallKeyCreator = new AccessLogCounterKeyCreator(false, false) {
            @Override
            public String counterKeyBaseName(AccessLogEntry entry) { return totalCounterName; }
        };
        logFileParser.addProcessor(new AccessLogCounterProcessor(totalStorePair, overallKeyCreator));
        return totalStorePair;
    }
}

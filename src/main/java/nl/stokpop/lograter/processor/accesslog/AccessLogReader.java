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
package nl.stokpop.lograter.processor.accesslog;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.clickpath.ClickPathAnalyser;
import nl.stokpop.lograter.clickpath.ClickPathAnalyserEngine;
import nl.stokpop.lograter.clickpath.InMemoryClickpathCollector;
import nl.stokpop.lograter.command.CommandAccessLog;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.feeder.FeedProcessor;
import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.logentry.ApacheLogMapperFactory;
import nl.stokpop.lograter.logentry.NginxLogMapperFactory;
import nl.stokpop.lograter.logentry.UrlSplitter;
import nl.stokpop.lograter.parser.AccessLogParser;
import nl.stokpop.lograter.parser.LogFileParser;
import nl.stokpop.lograter.parser.line.*;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.LogRaterUtils;
import nl.stokpop.lograter.util.SessionIdParser;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.linemapper.LineMap;
import nl.stokpop.lograter.util.linemapper.LineMapperUtils;
import nl.stokpop.lograter.util.time.SessionDurationCalculator;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Read access and nginx logs.
 */
public class AccessLogReader {

    private static final Logger log = LoggerFactory.getLogger(AccessLogReader.class);
    public static final String COMMON_LOG_PATTERN_APACHE = "%h %l %u %t \"%r\" %>s %b";
    public static final String COMMON_LOG_PATTERN_NGINX = "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\"";

    public AccessLogDataBundle readAndProcessAccessLogs(AccessLogConfig config, FeedProcessor feeder) {

        RequestCounterStoreFactory csFactory =
		        new RequestCounterStoreFactory(config.getCounterStorage(), config.getFilterPeriod(), new File(config.getCounterStorageDir()));

        List<AccessLogUrlMapperProcessor> urlMapperProcessors = LineMapperUtils.createUrlMapperProcessors(csFactory, config);

        final CommandAccessLog.LogType logType = config.getLogType();

        final UrlSplitter urlSplitter = config.isRemoveParametersFromUrl() ? AccessLogEntry.URL_SPLITTER_DEFAULT : null;

        AccessLogUserSessionProcessor userSessionProcessor = null;
        if (config.isDetermineSessionDurationEnabled()) {
            if (LogRaterUtils.isEmpty(config.getSessionField())) {
                throw new LogRaterException("If user session duration need to be determined, then supply a session field");
            }
            SessionDurationCalculator calculator = new SessionDurationCalculator();
            userSessionProcessor = new AccessLogUserSessionProcessor(calculator);
        }

        final AccessLogClickPathProcessor clickPathProcessor;
        final InMemoryClickpathCollector clickPathCollector;

        if (config.isDetermineClickpathsEnabled()) {
            if (LogRaterUtils.isEmpty(config.getSessionField())) {
                throw new LogRaterException("If clickpaths need to be determined, then supply a session field");
            }
            clickPathCollector = new InMemoryClickpathCollector();
            ClickPathAnalyser clickPathAnalyser = new ClickPathAnalyserEngine(clickPathCollector, config.getClickpathEndOfSessionSnippet());
            // TODO: using default mappers now... or rather the first mapper table only
            clickPathProcessor = new AccessLogClickPathProcessor(clickPathAnalyser, config.getLineMappers().get(0));
        }
        else {
            clickPathProcessor = null;
            clickPathCollector = null;
        }

        LogFormatParser<AccessLogEntry> lineParser;

        if (logType == CommandAccessLog.LogType.apache) {
            String pattern = StringUtils.useDefaultOrGivenValue(COMMON_LOG_PATTERN_APACHE, config.getLogPattern());

            List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);

            Map<String, LogEntryMapper<AccessLogEntry>> mappers =
                    ApacheLogMapperFactory.initializeMappers(elements, urlSplitter, config.getBaseUnit());
            lineParser =
                    new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);
        }
        else if (logType == CommandAccessLog.LogType.nginx){
            String pattern = StringUtils.useDefaultOrGivenValue(COMMON_LOG_PATTERN_NGINX, config.getLogPattern());

            List<LogbackElement> elements = NginxLogFormatParser.parse(pattern);
            Map<String, LogEntryMapper<AccessLogEntry>> mappers =
                    NginxLogMapperFactory.initializeMappers(elements, urlSplitter);
            lineParser =
                    new NginxLogFormatParser<>(elements, mappers, AccessLogEntry::new);
        }
        else {
            String msg = "Unsupported log type:" + logType;
            log.error(msg);
            throw new LogRaterException(msg);
        }

        final AccessLogParser accessLogParser = config.isDetermineSessionDurationEnabled() || config.isDetermineClickpathsEnabled()
            ? new AccessLogParser(lineParser, config.getFilterPeriod(), new SessionIdParser(config.getSessionField(), config.getSessionFieldRegexp()))
            : new AccessLogParser(lineParser, config.getFilterPeriod());

        if (config.isDetermineClickpathsEnabled()) {
            accessLogParser.addProcessor(clickPathProcessor);
        }
        if (config.isDetermineSessionDurationEnabled()) {
            accessLogParser.addProcessor(userSessionProcessor);
        }

        List<RequestCounterStorePair> requestCounterStoresPairs = new ArrayList<>();

        for (AccessLogUrlMapperProcessor urlMapperProcessor : urlMapperProcessors) {
            accessLogParser.addProcessor(urlMapperProcessor);
	        RequestCounterStore storeSuccess = urlMapperProcessor.getMappersRequestCounterStoreSuccess();
	        RequestCounterStore storeFailure = urlMapperProcessor.getMappersRequestCounterStoreFailure();
	        RequestCounterStorePair storePair = new RequestCounterStorePair(storeSuccess, storeFailure);
	        requestCounterStoresPairs.add(storePair);
        }

        int additionalColumns = 0;
        additionalColumns = additionalColumns + config.getGroupByFields().size();

        final String totalCounterName = RequestCounter.createCounterNameThatAlignsInTextReport("TOTAL", additionalColumns);

        RequestCounterStorePair totalRequestCounterStorePair =
		        addTotalRequestCounterStoreToLogFileParser(csFactory, accessLogParser, totalCounterName, config.getMaxUniqueCounters());

        requestCounterStoresPairs.addAll(createAccessLogCounterProcessors(accessLogParser, config, csFactory));

        feeder.feed(accessLogParser);

        if (clickPathProcessor != null) {
            clickPathProcessor.getClickPathAnalyser().closeAllRemainingSessions();
        }

	    if (config.isDetermineSessionDurationEnabled() && userSessionProcessor != null) {
		    SessionDurationCalculator calculator = userSessionProcessor.getSessionDurationCalculator();
		    long avgSessionDuration = calculator.getAvgSessionDuration();
		    TimePeriod avgSessionDurationPeriod = TimePeriod.createExcludingEndTime(0, avgSessionDuration);
		    log.info("Avg user session duration: {} ms ({})", avgSessionDuration, avgSessionDurationPeriod.getHumanReadableDuration());
	    }
	    else {
		    log.info("Avg user session duration calculation is disabled.");
	    }

        RequestCounter totalRequestCounterSuccess = totalRequestCounterStorePair.getRequestCounterStoreSuccess().getTotalRequestCounter();
        RequestCounter totalRequestCounterFailure = totalRequestCounterStorePair.getRequestCounterStoreFailure().getTotalRequestCounter();

        log.info("Read [{}] successful and [{}] failed access log entries for the following time period: [{}]",
		        totalRequestCounterSuccess.getHits(), totalRequestCounterFailure.getHits(), totalRequestCounterSuccess.getTimePeriod());

        Map<CounterKey, LineMap> allKeysToLineMap = new HashMap<>();
        for (AccessLogUrlMapperProcessor urlMapperProcessor : urlMapperProcessors) {
            Map<CounterKey, LineMap> counterKeyToLineMapMap = urlMapperProcessor.getKeyToLineMap();
            allKeysToLineMap.putAll(counterKeyToLineMapMap);
        }

        return clickPathCollector == null ?
            new AccessLogDataBundle(config, requestCounterStoresPairs, totalRequestCounterStorePair) :
            new AccessLogDataBundle(config, requestCounterStoresPairs, totalRequestCounterStorePair, clickPathCollector, allKeysToLineMap);
    }

    public static List<RequestCounterStorePair> createAccessLogCounterProcessors(
            final LogFileParser<AccessLogEntry> accessLogParser,
            final AccessLogConfig config,
            final RequestCounterStoreFactory csFactory) {

    	List<RequestCounterStorePair> requestCounterStorePairs = new ArrayList<>();

        if (config.isShowBasicUrls()) {
            RequestCounterStore urlCounterStoreSuccess = csFactory.newInstance("url-success", config.getMaxUniqueCounters());
            RequestCounterStore urlCounterStoreFailure = csFactory.newInstance("url-failure", config.getMaxUniqueCounters());

            RequestCounterStorePair urlStorePair = new RequestCounterStorePair(urlCounterStoreSuccess, urlCounterStoreFailure);

            AccessLogCounterKeyCreator keyCreator = new AccessLogCounterKeyCreator(config.getGroupByFields()) {
                @Override public String counterKeyBaseName(AccessLogEntry entry) { return entry.getUrl(); }
            };
            accessLogParser.addProcessor(new AccessLogCounterProcessor(urlStorePair, keyCreator));
            requestCounterStorePairs.add(urlStorePair);
        }
        if (config.isShowUserAgents()) {
            RequestCounterStore userAgentCounterStoreSuccess = csFactory.newInstance("userAgent-success", config.getMaxUniqueCounters());
            RequestCounterStore userAgentCounterStoreFailure = csFactory.newInstance("userAgent-failure", config.getMaxUniqueCounters());

            RequestCounterStorePair userAgentStorePair = new RequestCounterStorePair(userAgentCounterStoreSuccess, userAgentCounterStoreFailure);

            AccessLogCounterKeyCreator keyCreator = new AccessLogCounterKeyCreator(config.getGroupByFields()) {
                @Override public String counterKeyBaseName(AccessLogEntry entry) { return entry.getUserAgent(); }
            };
            accessLogParser.addProcessor(new AccessLogCounterProcessor(userAgentStorePair, keyCreator));
            requestCounterStorePairs.add(userAgentStorePair);
        }
        if (config.isShowReferers()) {
            RequestCounterStore referersCounterStoreSuccess = csFactory.newInstance("referer-success", config.getMaxUniqueCounters());
            RequestCounterStore referersCounterStoreFailure = csFactory.newInstance("referer-failure", config.getMaxUniqueCounters());

            RequestCounterStorePair referersStorePair = new RequestCounterStorePair(referersCounterStoreSuccess, referersCounterStoreFailure);

            AccessLogCounterKeyCreator keyCreator = new AccessLogCounterKeyCreator(config.getGroupByFields()) {
                @Override public String counterKeyBaseName(AccessLogEntry entry) { return entry.getReferrer(); }
            };
            accessLogParser.addProcessor(new AccessLogCounterProcessor(referersStorePair, keyCreator));
            requestCounterStorePairs.add(referersStorePair);
        }
        return requestCounterStorePairs;
    }

    public static RequestCounterStorePair addTotalRequestCounterStoreToLogFileParser(RequestCounterStoreFactory csFactory, LogFileParser<AccessLogEntry> logFileParser, final String totalCounterName, int maxUniqueCounters) {
        RequestCounterStore totalCounterStoreSuccess = csFactory.newInstance(String.join("-", totalCounterName, "success"), maxUniqueCounters);
        RequestCounterStore totalCounterStoreFailure = csFactory.newInstance(String.join("-", totalCounterName, "failure"), maxUniqueCounters);

        RequestCounterStorePair totalStorePair = new RequestCounterStorePair(totalCounterStoreSuccess, totalCounterStoreFailure);

        AccessLogCounterKeyCreator overallKeyCreator = new AccessLogCounterKeyCreator() {
            @Override
            public String counterKeyBaseName(AccessLogEntry entry) { return totalCounterName; }
        };
        logFileParser.addProcessor(new AccessLogCounterProcessor(totalStorePair, overallKeyCreator));
        return totalStorePair;
    }
}

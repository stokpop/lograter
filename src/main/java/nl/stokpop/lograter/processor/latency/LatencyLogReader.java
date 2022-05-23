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
package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.clickpath.ClickPathAnalyser;
import nl.stokpop.lograter.clickpath.ClickPathAnalyserEngine;
import nl.stokpop.lograter.clickpath.InMemoryClickpathCollector;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.feeder.FeedProcessor;
import nl.stokpop.lograter.logentry.LatencyLogEntry;
import nl.stokpop.lograter.logentry.LogEntrySuccessFactor;
import nl.stokpop.lograter.parser.LatencyLogParser;
import nl.stokpop.lograter.parser.line.LogEntryFactory;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.parser.line.LogbackParser;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.linemapper.LineMap;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import nl.stokpop.lograter.util.linemapper.LineMapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Read Latency logs.
 */
public class LatencyLogReader {

    private static final Logger log = LoggerFactory.getLogger(LatencyLogReader.class);

    public LatencyLogDataBundle readAndProcessLatencyLogs(LatencyLogConfig config, FeedProcessor feedProcessor) {

        String logPattern = config.getLogPattern();
        LogbackParser<LatencyLogEntry> lineParser = createLatencyLogEntryLogbackParser(logPattern, config);

        RequestCounterStoreFactory csFactory = new RequestCounterStoreFactory(config.getCounterStorage());
        LatencyLogData data = new LatencyLogData(csFactory);

        // the first element is used in the mapper, should not be a ('random') field in the key
        List<String> groupByFieldsWithoutFirstElement = listWithoutFirstElement(config.getCounterFields());
        LatencyCounterKeyCreator keyCreator = new LatencyCounterKeyCreator(groupByFieldsWithoutFirstElement);

        List<LineMapperSection> lineMappers = config.getLineMappers();
        if (lineMappers.size() > 1) {
            log.warn("More than one line mapper section found ({}), only using first one for LatencyLogProcessor mappers.", lineMappers);
        }
        if (lineMappers.isEmpty()) {
            lineMappers = LineMapperSection.SINGLE_MAPPER;
        }

        LineMapperSection firstLineMapperSection = lineMappers.get(0);
        LatencyLogProcessor processor = new LatencyLogProcessor(data, config, keyCreator);
        LatencyLogParser latencyLogParser = new LatencyLogParser(lineParser);
        latencyLogParser.addProcessor(processor);

        List<LatencyMapperProcessor> urlMapperProcessors = LineMapperUtils.createUrlMapperProcessors(csFactory, config, keyCreator);
        List<RequestCounterStorePair> requestCounterStoresPairs = new ArrayList<>();
        for (LatencyMapperProcessor urlMapperProcessor : urlMapperProcessors) {
            latencyLogParser.addProcessor(urlMapperProcessor);
            RequestCounterStore storeSuccess = urlMapperProcessor.getMappersRequestCounterStoreSuccess();
            RequestCounterStore storeFailure = urlMapperProcessor.getMappersRequestCounterStoreFailure();
            RequestCounterStorePair storePair = new RequestCounterStorePair(storeSuccess, storeFailure);
            requestCounterStoresPairs.add(storePair);
        }

        LatencyLogClickPathProcessor clickPathProcessor = null;
        InMemoryClickpathCollector clickpathCollector = null;
        if (config.isDetermineClickpathsEnabled()) {
            clickpathCollector = new InMemoryClickpathCollector();
            ClickPathAnalyser clickPathAnalyser = new ClickPathAnalyserEngine(clickpathCollector, "logout.do");
            // TODO: now only the default mappers are used for clickpath processing. Maybe also have 1 clickpath analyser per mapper?
            clickPathProcessor = new LatencyLogClickPathProcessor(
                clickPathAnalyser, firstLineMapperSection, config.getSessionField(),
                config.getCounterFields(), config.getClickPathShortCodeLength());
            latencyLogParser.addProcessor(clickPathProcessor);
        }

        // TODO have filter read replacements from external file.
        //        LogLineReplacementFilter httpFilter = new LogLineReplacementFilter(
        //                "Example to remove http parameters after ?. in ; separated file.",
        //                "(.*);(http[s]?://.*)\\?.*?;(.*)",
        //                "$1;$2;$3");
        //        httpFilter.addFeeder(parser);
        // next pass the httpFilter to the feedLines method below!

        feedProcessor.feed(latencyLogParser);

        if (clickPathProcessor != null) {
            clickPathProcessor.getClickPathAnalyser().closeAllRemainingSessions();
        }

        log.info("Read {} Latency log entries for the following time period: {}",
            data.getTotalRequestCounter().getHits(), data.getTotalRequestCounter().getTimePeriod());

        Map<CounterKey, LineMap> allKeysToLineMap = new HashMap<>();
        for (LatencyMapperProcessor urlMapperProcessor : urlMapperProcessors) {
            Map<CounterKey, LineMap> keyToLineMap = urlMapperProcessor.getKeyToLineMap();
            allKeysToLineMap.putAll(keyToLineMap);
        }

        return new LatencyLogDataBundle(config, processor.getData(), requestCounterStoresPairs, clickpathCollector, allKeysToLineMap);
    }

    private static List<String> listWithoutFirstElement(List<String> list) {
        if (list.isEmpty()) return Collections.emptyList();
        return Collections.unmodifiableList(list.stream().skip(1).collect(Collectors.toList()));
    }

    public static LogbackParser<LatencyLogEntry> createLatencyLogEntryLogbackParser(String logPattern, LatencyLogConfig config) {
        List<LogbackElement> elements = LogbackParser.parse(logPattern);
        Map<String, LogEntryMapper<LatencyLogEntry>> mappers = LatencyLogEntry.initializeLatencyLogMappers(elements, config);

        LogEntrySuccessFactor<LatencyLogEntry> successFactor;

        String failureField = config.getFailureField();
        if (failureField == null || failureField.isEmpty()) {
            successFactor = LatencyLogEntry.ALWAYS_SUCCESS;
        }
        else {
            String failureFieldRegexp = config.getFailureFieldRegexp();
            Pattern failurePattern = failureFieldRegexp == null ? null : Pattern.compile(failureFieldRegexp, Pattern.CASE_INSENSITIVE);
            successFactor = LatencyLogEntry.successFactorInstance(config.getFailureField(), config.getFailureFieldType(), failurePattern);
        }

        LogEntryFactory<LatencyLogEntry> logEntryFactory = () -> new LatencyLogEntry(successFactor);
        return new LogbackParser<>(elements, mappers, logEntryFactory);
    }

}

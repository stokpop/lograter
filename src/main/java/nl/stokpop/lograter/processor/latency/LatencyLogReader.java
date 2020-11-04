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
import nl.stokpop.lograter.feeder.FileFeeder;
import nl.stokpop.lograter.logentry.LatencyLogEntry;
import nl.stokpop.lograter.parser.LatencyLogParser;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.parser.line.LogbackParser;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import nl.stokpop.lograter.util.linemapper.LineMapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Read Latency logs.
 */
public class LatencyLogReader {

    private final static Logger log = LoggerFactory.getLogger(LatencyLogReader.class);

    public LatencyLogDataBundle readAndProcessLatencyLogs(LatencyLogConfig config, List<File> files) throws IOException {

        String logPattern = config.getLogPattern();
        LogbackParser<LatencyLogEntry> lineParser = createLatencyLogEntryLogbackParser(logPattern, config);

        RequestCounterStoreFactory csFactory = new RequestCounterStoreFactory(config.getCounterStorage());

        LatencyLogData data = new LatencyLogData(csFactory);

        LatencyLogProcessor processor = new LatencyLogProcessor(data, config);

        LatencyLogParser parser = new LatencyLogParser(lineParser);
        parser.addProcessor(processor);

        LatencyLogClickPathProcessor clickPathProcessor = null;
        InMemoryClickpathCollector clickpathCollector = null;
        if (config.isDetermineClickpaths()) {
            List<LineMapperSection> lineMappers = LineMapperUtils.createLineMapper(config.getMapperFile());
            clickpathCollector = new InMemoryClickpathCollector();
            ClickPathAnalyser clickPathAnalyser = new ClickPathAnalyserEngine(clickpathCollector, "logout.do");
            // TODO: now only the default mappers are used for clickpath processing.
            // Maybe also have 1 clickpath analyser per mapper?
            clickPathProcessor = new LatencyLogClickPathProcessor(clickPathAnalyser, lineMappers.get(0), config.getSessionField(), config.getCounterFields(), config.getClickPathShortCodeLength());
            parser.addProcessor(clickPathProcessor);
        }

        // TODO have filter read replacements from external file.
        //        LogLineReplacementFilter httpFilter = new LogLineReplacementFilter(
        //                "Example to remove http parameters after ?. in ; separated file.",
        //                "(.*);(http[s]?://.*)\\?.*?;(.*)",
        //                "$1;$2;$3");
        //        httpFilter.addFeeder(parser);
        // next pass the httpFilter to the feedLines method below!

        FileFeeder feeder = new FileFeeder(config.getFileFeederFilterIncludes(), config.getFileFeederFilterExcludes());
        feeder.feedFiles(files, parser);

        if (clickPathProcessor != null) {
            clickPathProcessor.getClickPathAnalyser().closeAllRemainingSessions();
        }

        log.info("Read {} Latency log entries for the following time period: {}", data.getTotalRequestCounter().getHits(), data.getTotalRequestCounter().getTimePeriod());

        return new LatencyLogDataBundle(config, data, clickpathCollector);
    }

    public static LogbackParser<LatencyLogEntry> createLatencyLogEntryLogbackParser(String logPattern, LatencyLogConfig config) {
        List<LogbackElement> elements = LogbackParser.parse(logPattern);
        Map<String, LogEntryMapper<LatencyLogEntry>> mappers = LatencyLogEntry.initializeLatencyLogMappers(elements, config);
        return new LogbackParser<>(elements, mappers, LatencyLogEntry::new);
    }
}

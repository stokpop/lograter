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
package nl.stokpop.lograter.processor.jmeter;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.feeder.FileFeeder;
import nl.stokpop.lograter.parser.JMeterParser;
import nl.stokpop.lograter.parser.LogFileParser;
import nl.stokpop.lograter.parser.line.JMeterLogFormatParser;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.linemapper.LineMapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JMeterLogReader {
    private final static Logger log = LoggerFactory.getLogger(JMeterLogReader.class);

    public JMeterDataBundle readAndProcessJMeterLogs(JMeterConfig config, List<String> files) {
        JMeterParser jMeterParser = createJMeterParser(files, config.getLogPattern(), config.getLogLineTypeToReport());

        RequestCounterStoreFactory csFactory = new RequestCounterStoreFactory(config.getCounterStorage(), new File(config.getCounterStorageDir()));

        List<JMeterUrlMapperProcessor> urlMapperProcessors = LineMapperUtils.createUrlMapperProcessors(csFactory, config);

        RequestCounterStorePair totalStorePair = addTotalRequestCounterStoreToLogFileParser(csFactory, jMeterParser, "total-counter");

        // collect the results
        List<RequestCounterStorePair> requestCounterStorePairs = new ArrayList<>();

        for (JMeterUrlMapperProcessor urlMapperProcessor : urlMapperProcessors) {
            jMeterParser.addProcessor(urlMapperProcessor);
            RequestCounterStore storeSuccess = urlMapperProcessor.getMappersRequestCounterStoreSuccess();
            RequestCounterStore storeFailure = urlMapperProcessor.getMappersRequestCounterStoreFailure();

            RequestCounterStorePair storePair = new RequestCounterStorePair(storeSuccess, storeFailure);
            requestCounterStorePairs.add(storePair);
        }

        FileFeeder feeder = new FileFeeder(config.getFileFeederFilterIncludes(), config.getFileFeederFilterExcludes(), 1);
        feeder.feedFilesAsString(files, jMeterParser);

        RequestCounter totalSuccess = totalStorePair.getRequestCounterStoreSuccess().getTotalRequestCounter();
        RequestCounter totalFailure = totalStorePair.getRequestCounterStoreFailure().getTotalRequestCounter();
        if (totalSuccess == null && totalFailure == null) {
            throw new LogRaterException("No lines (success or failure) processed in file feeder. Please check input parameters.");
        }

        return new JMeterDataBundle(config, requestCounterStorePairs, totalStorePair);
    }

    private JMeterParser createJMeterParser(List<String> files, String logPattern, JMeterLogLineType jmeterLogLineTypesToReport) {
        String relativeFilePath = files.get(0);
        String headerLine;
        try {
            headerLine = FileUtils.readFirstLine(relativeFilePath);
        } catch (IOException e) {
            throw new LogRaterException(String.format("Cannot read header line of file [%s]", relativeFilePath));
        }

        String pattern = StringUtils.useDefaultOrGivenValue(
                headerLine,
                logPattern);

        log.info("Using jmeter jtl pattern [{}] from [{}].", pattern, logPattern == null ? "file header" : "command line pattern");

        List<LogbackElement> elements = JMeterLogFormatParser.parse(pattern);
        Map<String, LogEntryMapper<JMeterLogEntry>> mappers = JMeterLogFormatParser.initializeMappers();
        JMeterLogFormatParser lineParser = new JMeterLogFormatParser(elements, mappers);

        return new JMeterParser(lineParser, jmeterLogLineTypesToReport);
    }

    private RequestCounterStorePair addTotalRequestCounterStoreToLogFileParser(RequestCounterStoreFactory csFactory,
                                                                               LogFileParser<JMeterLogEntry> logFileParser,
                                                                               String totalCounterName) {
        RequestCounterStore totalCounterStoreSuccess = csFactory.newInstance(String.join("-", totalCounterName, "success"));
        RequestCounterStore totalCounterStoreFailure = csFactory.newInstance(String.join("-", totalCounterName, "failure"));

        RequestCounterStorePair totalStorePair = new RequestCounterStorePair(totalCounterStoreSuccess, totalCounterStoreFailure);

        JMeterCounterKeyCreator keyCreator = new JMeterCounterKeyCreator(false) {
            @Override
            public String counterKeyBaseName(JMeterLogEntry entry) { return "total-counter"; }
        };

        logFileParser.addProcessor(new JMeterLogCounterProcessor(totalStorePair, keyCreator));

        return totalStorePair;
    }
}
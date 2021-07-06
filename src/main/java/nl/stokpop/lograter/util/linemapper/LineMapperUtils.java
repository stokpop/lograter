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
package nl.stokpop.lograter.util.linemapper;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.processor.accesslog.AccessLogConfig;
import nl.stokpop.lograter.processor.accesslog.AccessLogCounterKeyCreator;
import nl.stokpop.lograter.processor.accesslog.AccessLogUrlMapperProcessor;
import nl.stokpop.lograter.processor.jmeter.JMeterConfig;
import nl.stokpop.lograter.processor.jmeter.JMeterCounterKeyCreator;
import nl.stokpop.lograter.processor.jmeter.JMeterUrlMapperProcessor;
import nl.stokpop.lograter.processor.latency.LatencyCounterKeyCreator;
import nl.stokpop.lograter.processor.latency.LatencyLogConfig;
import nl.stokpop.lograter.processor.latency.LatencyMapperProcessor;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LineMapperUtils {

    private static final Logger log = LoggerFactory.getLogger(LineMapperUtils.class);

    public static List<AccessLogUrlMapperProcessor> createUrlMapperProcessors(
    		RequestCounterStoreFactory csFactory, AccessLogConfig config) {

        List<LineMapperSection> lineMappers = config.getLineMappers();

        List<AccessLogUrlMapperProcessor> processors = new ArrayList<>();

        for (LineMapperSection lineMapper : lineMappers) {
            RequestCounterStore mappersSuccess = csFactory.newInstance(lineMapper.getName() + "-mappers-success", "Mappers-Total-Success", config.getMaxUniqueRequests());
            RequestCounterStore mappersFailure = csFactory.newInstance(lineMapper.getName() + "-mappers-failure", "Mappers-Total-Failure", config.getMaxUniqueRequests());
            AccessLogCounterKeyCreator keyCreator = new AccessLogCounterKeyCreator(config.groupByHttpMethod(), config.groupByHttpStatus(), config.getGroupByFields());
            AccessLogUrlMapperProcessor processor =
                    new AccessLogUrlMapperProcessor(new RequestCounterStorePair(mappersSuccess, mappersFailure), lineMapper, keyCreator,
                            config.countNoMappersAsOne(), config.ignoreMultiAndNoMatches(), config.countMultipleMapperHits());
            processors.add(processor);
        }
        return processors;
    }

    public static List<LatencyMapperProcessor> createUrlMapperProcessors(
    		RequestCounterStoreFactory csFactory, LatencyLogConfig config) {

        List<LineMapperSection> lineMappers = config.getLineMappers();

        List<LatencyMapperProcessor> processors = new ArrayList<>();

        // the first item of counter fields is already present as mapper base value
        List<String> groupByFields = listWithoutFirstElement(config.getCounterFields());

        for (LineMapperSection lineMapper : lineMappers) {
            RequestCounterStore mappersSuccess = csFactory.newInstance(lineMapper.getName() + "-mappers-success", "Mappers-Total-Success", config.getMaxUniqueRequests());
            RequestCounterStore mappersFailure = csFactory.newInstance(lineMapper.getName() + "-mappers-failure", "Mappers-Total-Failure", config.getMaxUniqueRequests());
            LatencyCounterKeyCreator keyCreator = new LatencyCounterKeyCreator(groupByFields);
            LatencyMapperProcessor processor =
                    new LatencyMapperProcessor(new RequestCounterStorePair(mappersSuccess, mappersFailure), lineMapper, keyCreator,
                            config.countNoMappersAsOne(), config.ignoreMultiAndNoMatches(), config.countMultipleMapperHits());
            processors.add(processor);
        }
        return processors;
    }

    private static List<String> listWithoutFirstElement(List<String> list) {
        if (list.size() < 1) return Collections.emptyList();
        return Collections.unmodifiableList(list.stream().skip(1).collect(Collectors.toList()));
    }

    public static List<JMeterUrlMapperProcessor> createUrlMapperProcessors(
            RequestCounterStoreFactory csFactory, JMeterConfig config) {

        List<LineMapperSection> lineMappers = config.getLineMappers();

        List<JMeterUrlMapperProcessor> processors = new ArrayList<>();

        for (LineMapperSection lineMapper : lineMappers) {
            RequestCounterStore mappersSuccess = csFactory.newInstance(lineMapper.getName() + "-mappers-success", "Mappers-Total-Success", config.getMaxUniqueRequests());
            RequestCounterStore mappersFailure = csFactory.newInstance(lineMapper.getName() + "-mappers-failure", "Mappers-Total-Failure", config.getMaxUniqueRequests());
            JMeterCounterKeyCreator keyCreator = new JMeterCounterKeyCreator(config.groupByHttpStatus(), config.getGroupByFields());
            JMeterUrlMapperProcessor processor =
                    new JMeterUrlMapperProcessor(new RequestCounterStorePair(mappersSuccess, mappersFailure), lineMapper, keyCreator,
                            config.countNoMappersAsOne(), config.ignoreMultiAndNoMatches(), config.countMultipleMapperHits());
            processors.add(processor);
        }
        return processors;
    }

    public static List<LineMapperSection> createLineMapper(InputStream mapperConfigInputStream) throws IOException {
        if (mapperConfigInputStream == null) {
            return defaultLineMapper();
        }
        LineMapperReader lineMapperReader = new LineMapperReader();
        return lineMapperReader.initializeMappers(mapperConfigInputStream);
    }

    public static List<LineMapperSection> createLineMapper(String mapperConfigFile) throws IOException {
        if (mapperConfigFile == null) {
            return defaultLineMapper();
        }
        File mapperFile = new File(mapperConfigFile);
        return createLineMapper(mapperFile);
    }

    public static List<LineMapperSection> createLineMapper(File mapperConfigFile) throws IOException {
        if (mapperConfigFile == null) {
            return defaultLineMapper();
        }

        if (!mapperConfigFile.exists()) {
            throw new LogRaterException("Mapper config file does not exist: " + mapperConfigFile);
        }
        LineMapperReader lineMapperReader = new LineMapperReader();
        return lineMapperReader.initializeMappers(mapperConfigFile);
    }

    @NotNull
    private static List<LineMapperSection> defaultLineMapper() {
        log.warn("No mapper config file provided, returning default LineMapper");
        List<LineMapperSection> lineMappers = new ArrayList<>();
        lineMappers.add(new LineMapperSection("Default line mapper"));
        return lineMappers;
    }

}

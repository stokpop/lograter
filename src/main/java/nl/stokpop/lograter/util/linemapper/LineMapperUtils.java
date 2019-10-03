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
package nl.stokpop.lograter.util.linemapper;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.processor.accesslog.AccessLogConfig;
import nl.stokpop.lograter.processor.accesslog.AccessLogCounterKeyCreator;
import nl.stokpop.lograter.processor.accesslog.AccessLogUrlMapperProcessor;
import nl.stokpop.lograter.processor.jmeter.JMeterConfig;
import nl.stokpop.lograter.processor.jmeter.JMeterCounterKeyCreator;
import nl.stokpop.lograter.processor.jmeter.JMeterUrlMapperProcessor;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LineMapperUtils {

    private static final Logger log = LoggerFactory.getLogger(LineMapperUtils.class);

    public static List<AccessLogUrlMapperProcessor> createUrlMapperProcessors(
    		RequestCounterStoreFactory csFactory, AccessLogConfig config) {

        List<LineMapperSection> lineMappers = config.getLineMappers();

        List<AccessLogUrlMapperProcessor> processors = new ArrayList<>();

        for (LineMapperSection lineMapper : lineMappers) {
            RequestCounterStore mappersSuccess = csFactory.newInstance(lineMapper.getName() + "-mappers-success", "Mappers-Total-Success");
            RequestCounterStore mappersFailure = csFactory.newInstance(lineMapper.getName() + "-mappers-failure", "Mappers-Total-Failure");
            AccessLogCounterKeyCreator keyCreator = new AccessLogCounterKeyCreator(config.groupByHttpMethod(), config.groupByHttpStatus(), config.getGroupByFields());
            AccessLogUrlMapperProcessor processor =
                    new AccessLogUrlMapperProcessor(new RequestCounterStorePair(mappersSuccess, mappersFailure), lineMapper, keyCreator,
                            config.countNoMappersAsOne(), config.ignoreMultiAndNoMatches(), config.countMultipleMapperHits(), config.getMaxNoMapperCount());
            processors.add(processor);
        }
        return processors;
    }

    public static List<JMeterUrlMapperProcessor> createUrlMapperProcessors(
            RequestCounterStoreFactory csFactory, JMeterConfig config) {

        List<LineMapperSection> lineMappers = config.getLineMappers();

        List<JMeterUrlMapperProcessor> processors = new ArrayList<>();

        for (LineMapperSection lineMapper : lineMappers) {
            RequestCounterStore mappersSuccess = csFactory.newInstance(lineMapper.getName() + "-mappers-success", "Mappers-Total-Success");
            RequestCounterStore mappersFailure = csFactory.newInstance(lineMapper.getName() + "-mappers-failure", "Mappers-Total-Failure");
            JMeterCounterKeyCreator keyCreator = new JMeterCounterKeyCreator(config.groupByHttpStatus(), config.getGroupByFields());
            JMeterUrlMapperProcessor processor =
                    new JMeterUrlMapperProcessor(new RequestCounterStorePair(mappersSuccess, mappersFailure), lineMapper, keyCreator,
                            config.countNoMappersAsOne(), config.ignoreMultiAndNoMatches(), config.countMultipleMapperHits(), config.getMaxNoMapperCount());
            processors.add(processor);
        }
        return processors;
    }

    public static List<LineMapperSection> createLineMapper(InputStream mapperConfigFile) throws IOException {
        LineMapperReader lineMapperReader = new LineMapperReader();
        return lineMapperReader.initializeMappers(mapperConfigFile);
    }

    public static List<LineMapperSection> createLineMapper(String mapperConfigFile) throws IOException {

        if (mapperConfigFile == null) {
            log.warn("No mapper config file provided, returning default LineMapper");
            List<LineMapperSection> lineMappers = new ArrayList<>();
            lineMappers.add(new LineMapperSection("Default line mapper"));
            return lineMappers;
        }
        File mapperFile = new File(mapperConfigFile);

        if (!mapperFile.exists()) {
            throw new LogRaterException("Mapper config file does not exist: " + mapperFile);
        }
        LineMapperReader lineMapperReader = new LineMapperReader();
        return lineMapperReader.initializeMappers(mapperFile);
    }

}

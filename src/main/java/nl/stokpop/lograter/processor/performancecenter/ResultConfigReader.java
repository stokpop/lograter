/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.processor.performancecenter;

import lombok.extern.slf4j.Slf4j;
import nl.stokpop.lograter.LogRaterException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ResultConfigReader {

    private static final String SETTING_AGG_SEC_GRAN = "AggSecGran";
    private static final String SETTING_SCENARIO_START_TIME = "ScenarioStartTime";
    private static final String SETTING_DAYLIGHT_SAVING_SECS_ADDITION = "DaylightSavingSecsAddition";
    private static final Set<String> KNOWN_LRA_KEYS =
            Set.of(
                    SETTING_AGG_SEC_GRAN,
                    SETTING_SCENARIO_START_TIME,
                    SETTING_DAYLIGHT_SAVING_SECS_ADDITION
            );

    public static ResultConfig read(Path unzippedResultsDir) throws IOException {
        final Path lreFile;
        try (Stream<Path> files = Files.walk(unzippedResultsDir)) {
            lreFile = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".lra"))
                    .findFirst()
                    .orElseThrow(() -> new IOException("No file found that ends with '.lra'"));
        }

        final Map<String, String> lraSettings;
        try (Stream<String> lines = Files.lines(lreFile)) {
            lraSettings = lines.map(line -> line.split("="))
                    .filter(parts -> KNOWN_LRA_KEYS.contains(parts[0]))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));
        }

        final ResultConfig.ResultConfigBuilder builder = ResultConfig.builder();
        Optional.ofNullable(lraSettings.get(SETTING_AGG_SEC_GRAN))
                .ifPresentOrElse(
                        value -> builder.aggSecGran(Integer.valueOf(value)),
                        () -> log.warn("Missing setting '{}' in '{}'", SETTING_AGG_SEC_GRAN, lreFile)
                );

        builder.scenarioStartTime(
                        Optional.ofNullable(lraSettings.get(SETTING_SCENARIO_START_TIME))
                                .map(Long::valueOf)
                                .orElseThrow(() -> new LogRaterException(
                                        String.format("Missing setting '%s' in '%s'", SETTING_SCENARIO_START_TIME, lreFile)
                                ))
                )
                .daylightSavingSecsAddition(
                        Optional.ofNullable(lraSettings.get(SETTING_DAYLIGHT_SAVING_SECS_ADDITION))
                                .map(Integer::valueOf)
                                .orElseThrow(() -> new LogRaterException(
                                        String.format("Missing setting '%s' in '%s'", SETTING_DAYLIGHT_SAVING_SECS_ADDITION, lreFile)
                                ))

                );

        return builder.build();
    }
}

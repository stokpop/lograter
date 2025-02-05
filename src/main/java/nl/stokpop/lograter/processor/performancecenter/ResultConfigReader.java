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
package nl.stokpop.lograter.processor.performancecenter;

import lombok.extern.slf4j.Slf4j;
import nl.stokpop.lograter.LogRaterException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ResultConfigReader {

    private static final String SETTING_AGG_SEC_GRAN = "AggSecGran";
    private static final String SETTING_SCENARIO_START_TIME = "ScenarioStartTime";
    private static final String SETTING_DAYLIGHT_SAVING_SECS_ADDITION = "DaylightSavingSecsAddition";
    private static final Set<String> KNOWN_LRA_SETTINGS =
            Set.of(
                    SETTING_AGG_SEC_GRAN,
                    SETTING_SCENARIO_START_TIME,
                    SETTING_DAYLIGHT_SAVING_SECS_ADDITION
            );
    private static final Set<String> REQUIRED_LRA_SETTINGS = KNOWN_LRA_SETTINGS;

    public static ResultConfig read(File unzippedResultsDir) throws IOException {
        if (unzippedResultsDir == null) {
            throw new LogRaterException("No unzipped results directory provided");
        }

        return read(unzippedResultsDir.toPath());
    }

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
                    .filter(parts -> KNOWN_LRA_SETTINGS.contains(parts[0]))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));
        }

        REQUIRED_LRA_SETTINGS.stream()
                .filter(key -> Objects.isNull(lraSettings.get(key)) || lraSettings.get(key).isBlank())
                .findAny()
                .ifPresent(missingSetting -> {
                    throw new LogRaterException(
                            String.format("Setting '%s' in '%s' is missing value", missingSetting, lreFile)
                    );
                });

        return ResultConfig.builder()
                .configPath(lreFile.getFileName())
                .aggSecGran(parseIntOrFail(lraSettings, SETTING_AGG_SEC_GRAN))
                .scenarioStartTime(parseLongOrFail(lraSettings, SETTING_SCENARIO_START_TIME))
                .daylightSavingSecsAddition(parseIntOrFail(lraSettings, SETTING_DAYLIGHT_SAVING_SECS_ADDITION))
                .build();
    }

    private static int parseIntOrFail(Map<String, String> lraSettings, String settingName) {
        final String settingValue = lraSettings.get(settingName);
        try {
            return Integer.parseInt(settingValue);
        } catch (NumberFormatException e) {
            throw new LogRaterException(String.format(
                    "Unable to parse setting '%s' as int: %s",
                    settingName, settingValue
            ), e);
        }
    }

    private static long parseLongOrFail(Map<String, String> lraSettings, String settingName) {
        final String settingValue = lraSettings.get(settingName);
        try {
            return Long.parseLong(settingValue);
        } catch (NumberFormatException e) {
            throw new LogRaterException(String.format(
                    "Unable to parse setting '%s' as long: %s",
                    settingName, settingValue
            ), e);
        }
    }
}

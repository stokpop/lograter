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

import nl.stokpop.lograter.LogRaterException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class AbstractPerformanceCenterResultsReaderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void calculateStartTimeSecEpochWithDayLightSavingActive() throws IOException {
        final long scenarioStartTime = 1512653687;
        final long daylightSavingSecsAddition = 3600;

        File stubLraFile = tempFolder.newFile("day-light-saving-active.lra");
        Files.write(
                stubLraFile.toPath(),
                String.format(
                        "ScenarioStartTime=%s\nDaylightSavingSecsAddition=%s\n",
                        scenarioStartTime, daylightSavingSecsAddition
                ).getBytes()
        );

        // The actual start time is 14:34:47 "Europe/Amsterdam" zone time (winter time)
        // date --date=@1512653687
        // Thu Dec  7 14:34:47 CET 2017

        final long expectedResult = scenarioStartTime - daylightSavingSecsAddition;
        long startTime = PerformanceCenterCalculator.calculateStartTimeSecEpoch(tempFolder.getRoot().toPath());
        assertEquals(expectedResult, startTime);
    }

    @Test
    public void calculateStartTimeSecEpochWithDayLightSavingInactive() throws IOException {
        final long scenarioStartTime = 1512653687;
        final long daylightSavingSecsAddition = 0;

        File stubLraFile = tempFolder.newFile("day-light-saving-inactive.lra");
        Files.write(
                stubLraFile.toPath(),
                String.format(
                        "ScenarioStartTime=%s\nDaylightSavingSecsAddition=%s\n",
                        scenarioStartTime, daylightSavingSecsAddition
                ).getBytes()
        );

        // The actual start time is 14:34:47 "Europe/Amsterdam" zone time (winter time)
        // date --date=@1512653687
        // Thu Dec  7 14:34:47 CET 2017

        long startTime = PerformanceCenterCalculator.calculateStartTimeSecEpoch(tempFolder.getRoot().toPath());
        assertEquals(scenarioStartTime, startTime);
    }

    @Test
    public void shouldFailIfScenarioStartTimeIsMissing() throws IOException {
        final long daylightSavingSecsAddition = 0;

        File stubLraFile = tempFolder.newFile("ScenarioStartTime-missing.lra");
        Files.write(
                stubLraFile.toPath(),
                String.format(
                        "ScenarioStartTime=\nDaylightSavingSecsAddition=%s\n",
                        daylightSavingSecsAddition
                ).getBytes()
        );

        try {
            PerformanceCenterCalculator.calculateStartTimeSecEpoch(tempFolder.getRoot().toPath());
        } catch (LogRaterException e) {
            assertEquals("Missing setting 'ScenarioStartTime' in '" + stubLraFile + "'", e.getMessage());
        }
    }

    @Test
    public void shouldFailIfDaylightSavingSecsAdditionIsMissing() throws IOException {
        final long scenarioStartTime = 1512653687;

        File stubLraFile = tempFolder.newFile("DaylightSavingSecsAddition-missing.lra");
        Files.write(
                stubLraFile.toPath(),
                String.format(
                        "ScenarioStartTime=%s\nDaylightSavingSecsAddition=\n",
                        scenarioStartTime
                ).getBytes()
        );

        try {
            PerformanceCenterCalculator.calculateStartTimeSecEpoch(tempFolder.getRoot().toPath());
        } catch (LogRaterException e) {
            assertEquals("Missing setting 'DaylightSavingSecsAddition' in '" + stubLraFile + "'", e.getMessage());
        }
    }
}
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PerformanceCenterCalculatorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File databaseFile;

    @Before
    public void setUp() throws Exception {
        databaseFile = tempFolder.newFile("any-database-file.db");
    }

    @Test
    public void calculateAggSecGran() throws IOException {
        final Integer originalValue = 123;

        File stubLraFile = tempFolder.newFile("AggSecGran.lra");
        Files.write(
                stubLraFile.toPath(),
                String.format(
                        "AggSecGran=%s\nScenarioStartTime=0\nDaylightSavingSecsAddition=0\n",
                        originalValue
                ).getBytes()
        );

        Integer actualValue = PerformanceCenterCalculator.determineAggregationPeriod(databaseFile);
        assertEquals(originalValue, actualValue);
    }

    @Test
    public void calculateStartTimeSecEpochWithDayLightSavingActive() throws IOException {
        final long scenarioStartTime = 1512653687;
        final long daylightSavingSecsAddition = 3600;

        File stubLraFile = tempFolder.newFile("day-light-saving-active.lra");
        Files.write(
                stubLraFile.toPath(),
                String.format(
                        "AggSecGran=0\nScenarioStartTime=%s\nDaylightSavingSecsAddition=%s\n",
                        scenarioStartTime, daylightSavingSecsAddition
                ).getBytes()
        );

        // The actual start time is 14:34:47 "Europe/Amsterdam" zone time (winter time)
        // date --date=@1512653687
        // Thu Dec  7 14:34:47 CET 2017

        final long expectedResult = scenarioStartTime - daylightSavingSecsAddition;
        long startTime = PerformanceCenterCalculator.calculateStartTimeSecEpoch(databaseFile);
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
                        "AggSecGran=0\nScenarioStartTime=%s\nDaylightSavingSecsAddition=%s\n",
                        scenarioStartTime, daylightSavingSecsAddition
                ).getBytes()
        );

        // The actual start time is 14:34:47 "Europe/Amsterdam" zone time (winter time)
        // date --date=@1512653687
        // Thu Dec  7 14:34:47 CET 2017

        long startTime = PerformanceCenterCalculator.calculateStartTimeSecEpoch(databaseFile);
        assertEquals(scenarioStartTime, startTime);
    }

    @Test
    public void shouldFailIfScenarioStartTimeValueIsMissing() throws IOException {
        File stubLraFile = tempFolder.newFile("ScenarioStartTime-missing.lra");
        Files.write(
                stubLraFile.toPath(),
                "AggSecGran=0\nScenarioStartTime=\nDaylightSavingSecsAddition=0\n".getBytes()
        );

        try {
            PerformanceCenterCalculator.calculateStartTimeSecEpoch(databaseFile);
        } catch (LogRaterException e) {
            assertEquals("Setting 'ScenarioStartTime' in '" + stubLraFile + "' is missing value", e.getMessage());
        }
    }

    @Test
    public void shouldFailIfDaylightSavingSecsAdditionValueIsMissing() throws IOException {
        File stubLraFile = tempFolder.newFile("DaylightSavingSecsAddition-missing.lra");
        Files.write(
                stubLraFile.toPath(),
                "AggSecGran=0\nScenarioStartTime=0\nDaylightSavingSecsAddition=\n".getBytes()
        );

        try {
            PerformanceCenterCalculator.calculateStartTimeSecEpoch(databaseFile);
        } catch (LogRaterException e) {
            assertEquals("Setting 'DaylightSavingSecsAddition' in '" + stubLraFile + "' is missing value", e.getMessage());
        }
    }

    @Test
    public void shouldFailIfAggSecGranValueIsMissing() throws IOException {
        File stubLraFile = tempFolder.newFile("AggSecGran-missing.lra");
        Files.write(
                stubLraFile.toPath(),
                "AggSecGran=\nScenarioStartTime=0\nDaylightSavingSecsAddition=0\n".getBytes()
        );

        try {
            PerformanceCenterCalculator.calculateStartTimeSecEpoch(databaseFile);
        } catch (LogRaterException e) {
            assertEquals("Setting 'AggSecGran' in '" + stubLraFile + "' is missing value", e.getMessage());
        }
    }

    @Test
    public void createDataSet1() {
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(6);
        numbers.add(9);

        check(numbers);
    }

    @Test
    public void createDataSet2() {
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(6);
        numbers.add(4);
        numbers.add(9);
        check(numbers);
    }

    @Test
    public void createDataSet3() {

        List<Integer> numbers = new ArrayList<>();
        numbers.add(10);
        numbers.add(1000);
        numbers.add(400);
        numbers.add(500);
        numbers.add(600);
        numbers.add(200);
        numbers.add(400);
        numbers.add(800);
        numbers.add(900);
        numbers.add(400);
        numbers.add(100);
        numbers.add(800);

        check(numbers);

    }

    private void check(List<Integer> numbers) {

        Integer min = numbers.stream().min(Integer::compareTo).orElse(-1);
        Integer max = numbers.stream().max(Integer::compareTo).orElse(-2);

        double avgNumbers = numbers.stream().mapToDouble(Double::valueOf).average().orElse(-1);

        List<Double> dataSet = PerformanceCenterCalculator.createDataSet(numbers.size(), min, max, avgNumbers);

        assertEquals(numbers.size(), dataSet.size());

        double avgDataset = dataSet.stream().mapToDouble(Double::doubleValue).average().orElse(-2);

        assertEquals(avgNumbers, avgDataset, 0.0001d);
    }
}
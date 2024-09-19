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

import nl.stokpop.lograter.util.LogRaterRunTestUtil;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class ResultConfigFileReaderTest {

    @Test
    public void fetchAnalysisAggregationPeriodInSeconds() throws Exception {

        File resultsUnzippedDir = LogRaterRunTestUtil.convertTestResourceIntoFile(getClass(), "results-unzipped");

        int aggregationPeriodInSec = ResultConfigReader.read(resultsUnzippedDir.toPath().toAbsolutePath()).getAggSecGran();

        assertEquals(34, aggregationPeriodInSec);

    }

}
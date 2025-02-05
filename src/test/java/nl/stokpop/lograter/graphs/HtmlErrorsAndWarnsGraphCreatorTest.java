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
package nl.stokpop.lograter.graphs;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.store.TimeMeasurementStoreInMemory;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HtmlErrorsAndWarnsGraphCreatorTest {

    @Test
    public void createGraph() {
        RequestCounter errorsOverTime = new RequestCounter(CounterKey.of("Errors"), new TimeMeasurementStoreInMemory());
        errorsOverTime.incRequests(1000, 10);
        errorsOverTime.incRequests(5000, 10);
        errorsOverTime.incRequests(8000, 10);
        RequestCounter warnsOverTime = new RequestCounter(CounterKey.of("Warns"), new TimeMeasurementStoreInMemory());
        warnsOverTime.incRequests(1500, 20);
        warnsOverTime.incRequests(4500, 20);
        warnsOverTime.incRequests(9500, 20);

        String embeddedChartHtml = HtmlErrorsAndWarnsGraphCreator.createEmbeddedChartHtml(TimePeriod.createExcludingEndTime(0, 10000), errorsOverTime, warnsOverTime, 1000);
        assertTrue(embeddedChartHtml.contains("errors"));
    }

}
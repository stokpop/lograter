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
package nl.stokpop.lograter.processor;

import nl.stokpop.lograter.command.AbstractCommandBasic;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.processor.accesslog.AccessLogCounterKeyCreator;
import nl.stokpop.lograter.processor.accesslog.AccessLogUrlMapperProcessor;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import org.junit.Test;

import static nl.stokpop.lograter.command.AbstractCommandBasic.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccessLogUrlMapperProcessorTest {

    @Test
    public void testProcessEntry() {
        RequestCounterStoreFactory type = new RequestCounterStoreFactory(CounterStorageType.Memory);
        RequestCounterStore mappersSuccess = type.newInstance("mappers", MAX_UNIQUE_COUNTERS);
        RequestCounterStore mappersFailure = type.newInstance("mappers", MAX_UNIQUE_COUNTERS);
        LineMapperSection lineMapperSection = new LineMapperSection("linemappertable");
        lineMapperSection.addMapperRule("(.*)bar(.*)", "$1 $2 test");
        AccessLogCounterKeyCreator keyCreator = new AccessLogCounterKeyCreator();
        RequestCounterStorePair counterStorePair = new RequestCounterStorePair(mappersSuccess, mappersFailure);
        AccessLogUrlMapperProcessor accessLogUrlMapperProcessor = new AccessLogUrlMapperProcessor(
                counterStorePair,
                lineMapperSection,
                keyCreator,
                true,
                true,
                true);

        AccessLogEntry logEntry = new AccessLogEntry();
        logEntry.setUrl("onebartwo");
        accessLogUrlMapperProcessor.processEntry(logEntry);

	    AccessLogEntry logEntryFailed = new AccessLogEntry();
	    logEntryFailed.setUrl("onebartwo");
	    logEntryFailed.setHttpStatus(500);
	    accessLogUrlMapperProcessor.processEntry(logEntryFailed);

	    RequestCounterStore mappersRequestCounterStore = accessLogUrlMapperProcessor.getMappersRequestCounterStoreSuccess();
        RequestCounter requestCounter = mappersRequestCounterStore.get(CounterKey.of("one two test"));

        assertNotNull("request counter expected, it is null", requestCounter);
        assertEquals("one counter expected", 1, mappersSuccess.getCounterKeys().size());
        RequestCounter totalRequestCounter = mappersSuccess.getTotalRequestCounter();
        assertNotNull("one total counter expected", totalRequestCounter);
        assertEquals(1, totalRequestCounter.getHits());
    }
}
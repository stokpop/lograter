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
package nl.stokpop.lograter.report.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyser;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserFailureUnaware;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.CounterKeyMetaData;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.store.TimeMeasurementStoreInMemory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LogCounterJsonReportTest {

    private static final String UTF8 = StandardCharsets.UTF_8.name();
    final static private ObjectMapper mapper = new ObjectMapper();
    final static private JsonNodeFactory factory = new JsonNodeFactory(false);
    final static private JsonFactory jsonFactory = new JsonFactory();

    private RequestCounter counter;
    private ObjectNode reportRootNode;

    @Before
    public void init() {
        CounterKeyMetaData meta = new CounterKeyMetaData(Collections.singletonList("isThisInReport"), Collections.singletonList("boo"));
        this.counter = new RequestCounter(CounterKey.of("TestCounter", meta), new TimeMeasurementStoreInMemory());
        this.reportRootNode = factory.objectNode();
    }

    @Test
    public void testCreateCounterNode() throws Exception {
        final int TOTAL_HITS = 100;
        fillCounterWithSamples(counter, TOTAL_HITS);
        ResponseTimeAnalyser analyser = new ResponseTimeAnalyserFailureUnaware(counter);
        String data = createReport(reportRootNode, analyser, TOTAL_HITS, null);
        String percentile95 = "\"percentile95HitDurationMillis\":\"95\"";
        String percentile99 = "\"percentile99HitDurationMillis\":\"99\"";
        Assert.assertTrue(percentile95, data.contains(percentile95));
        Assert.assertTrue(percentile99, data.contains(percentile99));
        Assert.assertTrue("Counter key fields in report?", data.contains("isThisInReport"));
    }

    @Test
    public void testCreateCounterNode90Percentile() throws Exception {
        Double [] REPORT_90_PERCENTILE = new Double[] { 90.0D };
        final int TOTAL_HITS = 100;
        fillCounterWithSamples(counter, TOTAL_HITS);
        ResponseTimeAnalyser analyser = new ResponseTimeAnalyserFailureUnaware(counter);
        String data = createReport(reportRootNode, analyser, TOTAL_HITS, REPORT_90_PERCENTILE);
        String percentile90 = "\"percentile90HitDurationMillis\":\"90\"";
        Assert.assertTrue(percentile90, data.contains(percentile90));
    }

    @Test
    public void testCreateCounterNode90And999Percentile() throws Exception {

        final Double [] REPORT_90_AND_999_PERCENTILE = new Double[] { 90.0D, 99.9D };
        final int TOTAL_HITS = 1000;
        fillCounterWithSamples(counter, TOTAL_HITS);
        ResponseTimeAnalyser analyser = new ResponseTimeAnalyserFailureUnaware(counter);
        String data = createReport(reportRootNode, analyser, TOTAL_HITS, REPORT_90_AND_999_PERCENTILE);
        String percentile90 = "\"percentile90HitDurationMillis\":\"900\"";
        String percentile999 = "\"percentile99.9HitDurationMillis\":\"999\"";
        Assert.assertTrue(percentile90, data.contains(percentile90));
        Assert.assertTrue(percentile999, data.contains(percentile999));
    }

    /**
     * Fill the counter with samples increasing in duration: 1, 2, ... numberOfSamples
     * This will cause the average to be approximately be numberOfSamples / 2 (for larger numbers)
     * The timestamp will just also be ordered like the samples themselves 1, 2, ... numberOfSamples
     * @param counter to fill with samples
     * @param numberOfSamples the number of samples
     */
    private void fillCounterWithSamples(RequestCounter counter, int numberOfSamples) {
        for (int i = 1; i <= numberOfSamples; i++) {
            counter.incRequests(i, i);
        }
    }

    private String createReport(ObjectNode rootNode, ResponseTimeAnalyser analyser, int totalHits, Double [] reportPercentiles) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos, true, UTF8);
        JsonGenerator generator = jsonFactory.createGenerator(printStream);
        LogCounterJsonReport logCounterJsonReport = new LogCounterJsonReport();
        logCounterJsonReport.reportOverallCounter(rootNode, analyser, 0L, totalHits, reportPercentiles);
        mapper.writeTree(generator, rootNode);
        return baos.toString(UTF8);
    }

}
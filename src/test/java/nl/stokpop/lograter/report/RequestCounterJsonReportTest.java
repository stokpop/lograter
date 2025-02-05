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
package nl.stokpop.lograter.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.counter.RequestCounterDataBundle;
import nl.stokpop.lograter.processor.accesslog.AccessLogConfig;
import nl.stokpop.lograter.processor.accesslog.AccessLogDataBundle;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterAggregationGranularity;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterConfig;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterData;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterDataBundle;
import nl.stokpop.lograter.report.json.RequestCounterJsonReport;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.linemapper.LineMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.stokpop.lograter.processor.performancecenter.PerformanceCenterAggregationGranularity.AggregationGranularityType.DATABASE_GUESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RequestCounterJsonReportTest {

	public static final int MAX_UNIQUE_COUNTERS = 12;
	private final ObjectMapper mapper = new ObjectMapper();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testEmptyReport() throws Exception {

    	RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Memory);
	    RequestCounterStore requestCounterStoreSuccess = factory.newInstance("storeName", 12);
	    RequestCounterStore requestCounterStoreFailure = factory.newInstance("storeName", 12);

        RequestCounterStorePair totalPair = new RequestCounterStorePair(requestCounterStoreSuccess, requestCounterStoreFailure);

        AccessLogDataBundle bundle = createAccessLogDataBundle(Collections.emptyList(), totalPair);
        String result = createJsonReport(bundle);
//	    System.out.println(result);
	    assertTrue(result.contains("externalRunId"));
    }

	@Test
	public void testBigExternalSortCounterReport() throws Exception {

    	RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.ExternalSort, temporaryFolder.getRoot());

    	String storeName1 = "test-sub-store-1";

		RequestCounterStore store1 = factory.newInstance(storeName1, MAX_UNIQUE_COUNTERS);
		String storeName2 = "test-sub-store-2";
		RequestCounterStore store2 = factory.newInstance(storeName2, MAX_UNIQUE_COUNTERS);
		String totalStoreName = "total-store";
		RequestCounterStore totalStoreSuccess = factory.newInstance(String.join("-", totalStoreName, "success"), MAX_UNIQUE_COUNTERS);
		RequestCounterStore totalStoreFailure = factory.newInstance(String.join("-", totalStoreName, "failure"), MAX_UNIQUE_COUNTERS);

		RequestCounterStorePair totalPair = new RequestCounterStorePair(totalStoreSuccess, totalStoreFailure);

		int numberOfElements = 10000;
		fillStore(store1, totalStoreSuccess, numberOfElements);
		fillStore(store2, totalStoreSuccess, numberOfElements);

		RequestCounterStorePair pair1 = new RequestCounterStorePair(store1, factory.newInstance("failure1", MAX_UNIQUE_COUNTERS));
		RequestCounterStorePair pair2 = new RequestCounterStorePair(store2, factory.newInstance("failure2", MAX_UNIQUE_COUNTERS));
		List<RequestCounterStorePair> list = new ArrayList<>();
		list.add(pair1);
		list.add(pair2);
		AccessLogDataBundle bundle = createAccessLogDataBundle(list, totalPair);

		String result = createJsonReport(bundle);
		System.out.println(result);
		
        assertTrue("output should contain both store names", result.contains(storeName1) && result.contains(storeName2));
	}

	private void fillStore(RequestCounterStore store, RequestCounterStore total, int numberOfElements) {
		for (int elements = 0; elements < numberOfElements; elements++) {
			int timestamp = elements;
			CounterKey key = CounterKey.of("counter-key-" + timestamp % 10);
			int duration = timestamp % 5000;
			store.add(key, timestamp, duration);
			total.add(key, timestamp, duration);
			// double the load in last part, so the peak minute is in the last part
			double fivePercentOfElements = numberOfElements * 0.05;
			if (timestamp >= (numberOfElements - fivePercentOfElements)) {
				store.add(key, timestamp, duration);
				store.add(key, timestamp, duration);
			}
		}
	}


	@Test
    public void testOneCounterReport() throws Exception {
        RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Memory);
        String storeName1 = "test-sub-store-1";
        RequestCounterStore storeSuccess1 = factory.newInstance(storeName1, MAX_UNIQUE_COUNTERS);
        RequestCounterStore storeFailure1 = factory.newInstance(storeName1, MAX_UNIQUE_COUNTERS);
        String storeName2 = "test-sub-store-2";
        RequestCounterStore storeSuccess2 = factory.newInstance(storeName2, MAX_UNIQUE_COUNTERS);
        RequestCounterStore storeFailure2 = factory.newInstance(storeName2, MAX_UNIQUE_COUNTERS);

		CounterKey key1 = CounterKey.of("counter-key-1");
		CounterKey key2 = CounterKey.of("counter-key-2");

        storeSuccess1.add(key1, 1000, 1000);
        storeSuccess1.add(key1, 2000, 2000);
        storeSuccess1.add(key1, 3000, 3000);
        storeSuccess1.add(key2, 1001, 100);
        storeSuccess1.add(key2, 2001, 200);
        storeSuccess1.add(key2, 2950, 300);
        storeFailure1.add(key2, 2150, 3);

		storeSuccess2.add(key1, 1000, 1000);
		storeSuccess2.add(key1, 2000, 2000);
		storeSuccess2.add(key1, 3000, 3000);
		storeSuccess2.add(key2, 1001, 100);
		storeSuccess2.add(key2, 2001, 200);
		storeSuccess2.add(key2, 2950, 300);
		storeFailure2.add(key2, 2100, 2);

		RequestCounterStorePair requestCounterStorePair1 = new RequestCounterStorePair(storeSuccess1, storeFailure1);
		RequestCounterStorePair requestCounterStorePair2 = new RequestCounterStorePair(storeSuccess2, storeFailure2);

        List<RequestCounterStorePair> requestCounterStorePairs = new ArrayList<>();
		requestCounterStorePairs.add(requestCounterStorePair1);
		requestCounterStorePairs.add(requestCounterStorePair2);

		RequestCounterStorePair totalRequestCounterStorePair = new RequestCounterStorePair(storeSuccess1, storeFailure1);

		AccessLogDataBundle bundle = createAccessLogDataBundle(requestCounterStorePairs, totalRequestCounterStorePair);

        String result = createJsonReport(bundle);
        System.out.println(result);
        assertTrue(result.contains(storeName1) && result.contains(storeName2));
    }

	@Test
	public void testOneCounterReportPerformanceCenter() throws Exception {

		PerformanceCenterConfig config = new PerformanceCenterConfig();
		config.setRunId("test-run-1");

		PerformanceCenterData data = new PerformanceCenterData(
				new RequestCounterStoreFactory(CounterStorageType.Memory),
				new PerformanceCenterAggregationGranularity(1000, DATABASE_GUESS),
				MAX_UNIQUE_COUNTERS);

        RequestCounterStorePair pair = data.getRequestCounterStorePair();
        RequestCounterStore storeSuccess = pair.getRequestCounterStoreSuccess();
		RequestCounterStore storeFailure = pair.getRequestCounterStoreFailure();

		CounterKey key1 = CounterKey.of("counter-key-1");
		CounterKey key2 = CounterKey.of("counter-key-2");

		pair.addSuccess(key1, 1000, 1000);
		pair.addSuccess(key1, 2000, 2000);
		pair.addSuccess(key1, 3000, 3000);
		pair.addSuccess(key2, 1001, 100);
		pair.addSuccess(key2, 2001, 200);
		pair.addSuccess(key2, 2950, 300);

		pair.addFailure(key1, 1001, 1009);
		pair.addFailure(key1, 2002, 2009);
		pair.addFailure(key1, 3003, 3009);
		pair.addFailure(key2, 1004, 19);
		pair.addFailure(key2, 2005, 29);
		pair.addFailure(key2, 2956, 309);

		PerformanceCenterDataBundle performanceCenterDataBundle = new PerformanceCenterDataBundle(config, data);

		String result = createJsonReport(performanceCenterDataBundle);
		//System.out.println(result);

        assertTrue(result.contains("failures"));

        JsonNode rootNode = mapper.readTree(result);

        JsonNode overallCounterNode = rootNode.get("overall-counter");

        int overallHits = overallCounterNode.get("hits").asInt();
        int maxHitDurationMillis = overallCounterNode.get("maxHitDurationMillis").asInt();

        // for performance center the failed hits should not be included in the analysis
        assertEquals("6 success hits expected, not 12 because 6 are failed hits", 6, overallHits);
        assertEquals("3009 is not expected: that is for a failure", 3000, maxHitDurationMillis);

        JsonNode counterStores = rootNode.get("counterStores");

        List<Integer> maxDurationsInt = counterStores.findValues("maxHitDurationMillis")
                .stream()
                .map(JsonNode::asInt)
                .collect(Collectors.toList());

        assertTrue("should exclude failure max response time for each counter (3009 and 309)",
                (maxDurationsInt.contains(3000) && maxDurationsInt.contains(300))
                        && !(maxDurationsInt.contains(3009) || maxDurationsInt.contains(309)));
	}

	@Test
	public void allCountersArePresentTest() throws IOException {
        PerformanceCenterData data = new PerformanceCenterData(
                new RequestCounterStoreFactory(CounterStorageType.Memory),
                new PerformanceCenterAggregationGranularity(1000, DATABASE_GUESS),
				MAX_UNIQUE_COUNTERS);

        RequestCounterStorePair counterStorePair = data.getRequestCounterStorePair();
		CounterKey successKey = CounterKey.of("counter-key-success-only");
		CounterKey failureKey = CounterKey.of("counter-key-failure-only");
		CounterKey successAndFailureKey = CounterKey.of("counter-key-success-and-failure");

		counterStorePair.addSuccess(successKey, 1001, 100);
		counterStorePair.addSuccess(successAndFailureKey, 1001, 100);

		counterStorePair.addFailure(failureKey, 1000, 1000);
        counterStorePair.addFailure(successAndFailureKey, 2950, 300);

        PerformanceCenterDataBundle dataBundle = new PerformanceCenterDataBundle(new PerformanceCenterConfig(), data);

		String result = createJsonReport(dataBundle);

		assertEquals(3, StringUtils.countOccurrences(result.substring(result.indexOf("counters")), "name"));
	}

	private AccessLogDataBundle createAccessLogDataBundle(
			List<RequestCounterStorePair> requestCounterStorePairs, RequestCounterStorePair totalRequestCounterStorePair) {
		return createAccessLogDataBundle(requestCounterStorePairs, totalRequestCounterStorePair, Collections.emptyMap());
	}

	private AccessLogDataBundle createAccessLogDataBundle(
			List<RequestCounterStorePair> requestCounterStorePairs, RequestCounterStorePair totalRequestCounterPair, Map<CounterKey, LineMap> keyToLIneMap) {
		AccessLogConfig config = new AccessLogConfig();
		return new AccessLogDataBundle(config, requestCounterStorePairs, totalRequestCounterPair, keyToLIneMap);
	}

    private String createJsonReport(RequestCounterDataBundle bundle) throws IOException {
        RequestCounterJsonReport report = new RequestCounterJsonReport(bundle);
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			try (PrintWriter printStream = FileUtils.createBufferedPrintWriterWithUTF8(outputStream)) {
				report.report(printStream);
				printStream.flush();
			}
			return outputStream.toString(StandardCharsets.UTF_8.name());
		}
    }
}
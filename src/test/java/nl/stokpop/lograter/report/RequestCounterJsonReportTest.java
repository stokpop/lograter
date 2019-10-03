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
package nl.stokpop.lograter.report;

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
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.linemapper.LineMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static nl.stokpop.lograter.processor.performancecenter.PerformanceCenterAggregationGranularity.AggregationGranularityType.DATABASE_GUESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RequestCounterJsonReportTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testEmptyReport() throws Exception {

    	RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Memory);
	    RequestCounterStore requestCounterStoreSuccess = factory.newInstance("storeName");
	    RequestCounterStore requestCounterStoreFailure = factory.newInstance("storeName");

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
		RequestCounterStore store1 = factory.newInstance(storeName1);
		String storeName2 = "test-sub-store-2";
		RequestCounterStore store2 = factory.newInstance(storeName2);
		String totalStoreName = "total-store";
		RequestCounterStore totalStoreSuccess = factory.newInstance(String.join("-", totalStoreName, "success"));
		RequestCounterStore totalStoreFailure = factory.newInstance(String.join("-", totalStoreName, "failure"));

		RequestCounterStorePair totalPair = new RequestCounterStorePair(totalStoreSuccess, totalStoreFailure);

		int numberOfElements = 10000;
		fillStore(store1, totalStoreSuccess, numberOfElements);
		fillStore(store2, totalStoreSuccess, numberOfElements);

		RequestCounterStorePair pair1 = new RequestCounterStorePair(store1, factory.newInstance("failure1"));
		RequestCounterStorePair pair2 = new RequestCounterStorePair(store2, factory.newInstance("failure2"));
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
			String counterKey = "counter-key-" + timestamp % 10;
			int duration = timestamp % 5000;
			store.add(counterKey, timestamp, duration);
			total.add(counterKey, timestamp, duration);
			// double the load in last part, so the peak minute is in the last part
			double fivePercentOfElements = numberOfElements * 0.05;
			if (timestamp >= (numberOfElements - fivePercentOfElements)) {
				store.add(counterKey, timestamp, duration);
				store.add(counterKey, timestamp, duration);
			}
		}
	}


	@Test
    public void testOneCounterReport() throws Exception {
        RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Memory);
        String storeName1 = "test-sub-store-1";
        RequestCounterStore storeSuccess1 = factory.newInstance(storeName1);
        RequestCounterStore storeFailure1 = factory.newInstance(storeName1);
        String storeName2 = "test-sub-store-2";
        RequestCounterStore storeSuccess2 = factory.newInstance(storeName2);
        RequestCounterStore storeFailure2 = factory.newInstance(storeName2);

        storeSuccess1.add("counter-key-1", 1000, 1000);
        storeSuccess1.add("counter-key-1", 2000, 2000);
        storeSuccess1.add("counter-key-1", 3000, 3000);
        storeSuccess1.add("counter-key-2", 1001, 100);
        storeSuccess1.add("counter-key-2", 2001, 200);
        storeSuccess1.add("counter-key-2", 2950, 300);
        storeFailure1.add("counter-key-2", 2150, 3);

		storeSuccess2.add("counter-key-1", 1000, 1000);
		storeSuccess2.add("counter-key-1", 2000, 2000);
		storeSuccess2.add("counter-key-1", 3000, 3000);
		storeSuccess2.add("counter-key-2", 1001, 100);
		storeSuccess2.add("counter-key-2", 2001, 200);
		storeSuccess2.add("counter-key-2", 2950, 300);
		storeFailure2.add("counter-key-2", 2100, 2);

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
				new PerformanceCenterAggregationGranularity(1000, DATABASE_GUESS));

		RequestCounterStore storeSuccess = data.getRequestCounterStorePair().getRequestCounterStoreSuccess();
		RequestCounterStore storeFailure = data.getRequestCounterStorePair().getRequestCounterStoreFailure();

		storeSuccess.add("counter-key-1", 1000, 1000);
		storeSuccess.add("counter-key-1", 2000, 2000);
		storeSuccess.add("counter-key-1", 3000, 3000);
		storeSuccess.add("counter-key-2", 1001, 100);
		storeSuccess.add("counter-key-2", 2001, 200);
		storeSuccess.add("counter-key-2", 2950, 300);

		storeFailure.add("counter-key-1", 1000, 1000);
		storeFailure.add("counter-key-1", 2000, 2000);
		storeFailure.add("counter-key-1", 3000, 3000);
		storeFailure.add("counter-key-2", 1001, 100);
		storeFailure.add("counter-key-2", 2001, 200);
		storeFailure.add("counter-key-2", 2950, 300);

		PerformanceCenterDataBundle performanceCenterDataBundle = new PerformanceCenterDataBundle(config, data);

		String result = createJsonReport(performanceCenterDataBundle);
		System.out.println(result);
		assertTrue(result.contains("failures"));
	}

	@Test
	public void allCountersArePresentTest() throws IOException {
        PerformanceCenterData data = new PerformanceCenterData(
                new RequestCounterStoreFactory(CounterStorageType.Memory),
                new PerformanceCenterAggregationGranularity(1000, DATABASE_GUESS));

        RequestCounterStorePair counterStorePair = data.getRequestCounterStorePair();
        counterStorePair.addSuccess("counter-key-success-only", 1001, 100);
        counterStorePair.addSuccess("counter-key-success-and-failure", 1001, 100);

        counterStorePair.addFailure("counter-key-failure-only", 1000, 1000);
        counterStorePair.addFailure("counter-key-success-and-failure", 2950, 300);

        PerformanceCenterDataBundle dataBundle = new PerformanceCenterDataBundle(new PerformanceCenterConfig(), data);

		String result = createJsonReport(dataBundle);

		assertEquals(3, StringUtils.countOccurrences(result.substring(result.indexOf("counters")), "name"));
	}

	private AccessLogDataBundle createAccessLogDataBundle(
			List<RequestCounterStorePair> requestCounterStorePairs, RequestCounterStorePair totalRequestCounterStorePair) {
		return createAccessLogDataBundle(requestCounterStorePairs, totalRequestCounterStorePair, Collections.emptyMap());
	}

	private AccessLogDataBundle createAccessLogDataBundle(
			List<RequestCounterStorePair> requestCounterStorePairs, RequestCounterStorePair totalRequestCounterPair, Map<String, LineMap> counterKeyToLineMapMap) {
		AccessLogConfig config = new AccessLogConfig();
		return new AccessLogDataBundle(config, requestCounterStorePairs, totalRequestCounterPair, counterKeyToLineMapMap);
	}

    private String createJsonReport(RequestCounterDataBundle bundle) throws IOException {
        RequestCounterJsonReport report = new RequestCounterJsonReport(bundle);
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			try (PrintStream printStream = new PrintStream(outputStream)) {
				report.report(printStream);
				printStream.flush();
			}
			return outputStream.toString("UTF-8");
		}
    }
}
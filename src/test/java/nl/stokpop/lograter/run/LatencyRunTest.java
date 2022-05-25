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
package nl.stokpop.lograter.run;

import nl.stokpop.lograter.util.LogRaterRunTestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static junit.framework.TestCase.assertTrue;

public class LatencyRunTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
	public void testLatencyLogFile() throws Exception {
		String[] runArgs = {
			    "@src/test/resources/latency/latency.options",
				"src/test/resources/latency/latency.log"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		assertTrue("Contains text [Duration]", result.contains("Duration"));
		assertTrue("Contains text [NO_MAPPER]", result.contains("NO_MAPPER"));
		assertTrue("Contains text [99%]", result.contains("99%"));
		assertTrue("The log file contains 5 lines.", result.contains("latency-log-counterstore-success-total,5"));
		assertTrue("Both success and failure is counted for non mapper.", result.contains("NO_MAPPER-/hex/pah/col,2"));
	}

	@Test
	public void testLatencyLogFileThreeCounterFields() throws Exception {

    	// note that the first of the counter-fields is used for the mapper
		String[] runArgs = {
			"latency",
			"-fffi",
			"\\[RTR/.*HTTP",
			"--counter-fields",
			"operation,http-method,http-code",
			"-mf",
			"src/test/resources/latency/latency.mapper",
			"-latency-field",
			"response_time",
			"-latency-unit",
			"seconds",
			"-lp",
			"%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} [RTR/%X{rtr-nr}] OUT %X{host} - [%X{timestamp}] \"%X{http-method} %X{operation} %X{http-version}\" %X{http-code} %X{http-something} %X{http-bytes} \"%X{unknown1}\" \"%X{http-referer}\" \"%X{remote-ip}:%X{remote-port}\" \"%X{local-ip}:%X{local-port}\" x_forwarded_for:\"%X{x_forwarded_for}\" x_forwarded_proto:\"%X{x_forwarded_proto}\" vcap_request_id:\"%X{vcap_request_id}\" response_time:%X{response_time} gorouter_time:%X{gorouter_time} app_id:\"%X{app_id}\" app_index:\"%X{app_index}\" x_cf_routererror:\"%X{x_cf_routererror}\" x_client_ip:\"%X{x_client_ip}\" x_session_id:\"%X{x_session_id}\" x_b3_traceid:\"%X{x_b3_traceid}\" x_b3_spanid:\"%X{x_b3_spanid}\" x_b3_parentspanid:\"%X{x_b3_parentspanid}\" b3:\"%X{b3}\"",
			"src/test/resources/latency/latency.log"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		assertTrue("Contains text [Duration]", result.contains("Duration"));
		assertTrue("Contains text [NO_MAPPER]", result.contains("NO_MAPPER"));
		assertTrue("Contains text [99%]", result.contains("99%"));
		assertTrue("Contains text [ACME -- kab,GET,200]", result.contains("ACME -- kab,GET,200"));
		assertTrue("The log file contains 5 lines.", result.contains("latency-log-counterstore-success-total,TOTAL,TOTAL,5"));
	}

	@Test
	public void testLatencyLogFileFailureFields() throws Exception {

    	// note that the first of the counter-fields is used for the mapper
		String[] runArgs = {
			"latency",
			"-fffi",
			"\\[RTR/.*HTTP",
			"--counter-fields",
			"operation",
			"-mf",
			"src/test/resources/latency/latency.mapper",
			"-latency-field",
			"response_time",
			"-latency-unit",
			"seconds",
			"-failure-field",
			"http-code",
			"-lp",
			"%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} [RTR/%X{rtr-nr}] OUT %X{host} - [%X{timestamp}] \"%X{http-method} %X{operation} %X{http-version}\" %X{http-code} %X{http-something} %X{http-bytes} \"%X{unknown1}\" \"%X{http-referer}\" \"%X{remote-ip}:%X{remote-port}\" \"%X{local-ip}:%X{local-port}\" x_forwarded_for:\"%X{x_forwarded_for}\" x_forwarded_proto:\"%X{x_forwarded_proto}\" vcap_request_id:\"%X{vcap_request_id}\" response_time:%X{response_time} gorouter_time:%X{gorouter_time} app_id:\"%X{app_id}\" app_index:\"%X{app_index}\" x_cf_routererror:\"%X{x_cf_routererror}\" x_client_ip:\"%X{x_client_ip}\" x_session_id:\"%X{x_session_id}\" x_b3_traceid:\"%X{x_b3_traceid}\" x_b3_spanid:\"%X{x_b3_spanid}\" x_b3_parentspanid:\"%X{x_b3_parentspanid}\" b3:\"%X{b3}\"",
			"src/test/resources/latency/latency.log"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		assertTrue("Contains text [Duration]", result.contains("Duration"));
		assertTrue("Contains text [99%]", result.contains("99%"));
		assertTrue("Contains text [ACME -- kab,1,0]", result.contains("ACME -- kab,1,0"));
		assertTrue("Contains text [NO_MAPPER-/hex/pah/col,2,1]", result.contains("NO_MAPPER-/hex/pah/col,2,1"));
		assertTrue("The log file contains 5 lines and 1 failure.", result.contains("latency-log-counterstore-success-total,5,1"));
	}

	@Test
	public void testLatencyLogFileFailureFieldsAndHttpCodeMethodAndOverflow() throws Exception {

    	// note that the first of the counter-fields is used for the mapper
		String[] runArgs = {
			"latency",
			"-fffi",
			"\\[RTR/.*HTTP",
			"--counter-fields",
			"operation,http-code,http-method",
			"-mf",
			"src/test/resources/latency/latency.mapper",
			"-latency-field",
			"response_time",
			"-latency-unit",
			"seconds",
			"-failure-field",
			"http-code",
			"--max-unique-counters",
			"0",
			"-lp",
			"%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} [RTR/%X{rtr-nr}] OUT %X{host} - [%X{timestamp}] \"%X{http-method} %X{operation} %X{http-version}\" %X{http-code} %X{http-something} %X{http-bytes} \"%X{unknown1}\" \"%X{http-referer}\" \"%X{remote-ip}:%X{remote-port}\" \"%X{local-ip}:%X{local-port}\" x_forwarded_for:\"%X{x_forwarded_for}\" x_forwarded_proto:\"%X{x_forwarded_proto}\" vcap_request_id:\"%X{vcap_request_id}\" response_time:%X{response_time} gorouter_time:%X{gorouter_time} app_id:\"%X{app_id}\" app_index:\"%X{app_index}\" x_cf_routererror:\"%X{x_cf_routererror}\" x_client_ip:\"%X{x_client_ip}\" x_session_id:\"%X{x_session_id}\" x_b3_traceid:\"%X{x_b3_traceid}\" x_b3_spanid:\"%X{x_b3_spanid}\" x_b3_parentspanid:\"%X{x_b3_parentspanid}\" b3:\"%X{b3}\"",
			"src/test/resources/latency/latency-more.log"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		assertTrue("Contains text [Duration]", result.contains("Duration"));
		assertTrue("Contains text [99%]", result.contains("99%"));
		assertTrue("Contains text [OVERFLOW-COUNTER,200,GET,1,0]", result.contains("OVERFLOW-COUNTER,200,GET,1,0"));
		assertTrue("Contains text [OVERFLOW-COUNTER,500,POST,1,1,100.00]", result.contains("OVERFLOW-COUNTER,500,POST,1,1,100.00"));
		assertTrue("The log file contains 6 lines and 1 failure.", result.contains("latency-log-counterstore-success-total,TOTAL,TOTAL,6,1"));
	}
}

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
import static org.junit.Assert.assertFalse;

public class JmeterRunTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
	public void testJMeterJtlFile() throws Exception {
		String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
				"jmeter",
                "-gt",
                "-gh",
                "-gr",
                "-gp",
				"src/test/resources/jmeter/result_20180502_142434.jtl"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.println("[" + result + "]");

		assertTrue("Contains text [Duration]", result.contains("Duration"));
		assertTrue("Contains text [NO_MAPPER]", result.contains("NO_MAPPER"));
		assertTrue("Contains text [99%]", result.contains("99%"));
		assertTrue("The files contain 714 lines of which 0 failure lines.", result.contains("total-counter-success-total,714,0"));

	}

	@Test
	public void testJMeterJtlFileWithFailures() throws Exception {
		String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
				"jmeter",
                "-gt",
                "-gh",
                "-gr",
                "-gp",
                "-group-by-http-status",
				"src/test/resources/jmeter/result_with_500_and_no_text.jtl"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.println("[" + result + "]");

		assertTrue("Contains 1 error 500", result.contains("stubby1/show,500,1"));
		assertTrue("Contains 5 errors 404", result.contains("stubby3/show,404,5"));
		assertTrue("Contains 2 errors 503", result.contains("stubby3/show,503,2"));
		assertTrue("The files contain 29 lines of which 8 failure lines.", result.contains("total-counter-success-total,29,8"));

	}

	@Test
	public void testJMeterJtlFileWithSamplesAndTransactions() throws Exception {
		String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
				"jmeter",
			    "--report-logline-type",
			    "all",
                "-gt",
                "-gh",
                "-gr",
                "-gp",
                "-group-by-http-status",
				"src/test/resources/jmeter/result_with_url_and_sample_transaction_lines.jtl"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		// sampler samples (e.g. http request)
		assertTrue("Contains 3 GET Jaw Kat", result.contains("GET Jaw Kat,200,3"));
		assertTrue("Contains 11 GET Jaw Yaw", result.contains("GET Jaw Yaw,200,11"));
		// transaction controller samples
		assertTrue("Contains 3 cog", result.contains("cog,200,3"));
		assertTrue("Contains 12 som", result.contains("som,200,12"));

		assertTrue("The files contain 29 lines of which 0 failure lines.", result.contains("total-counter-success-total,29,0"));
	}

	@Test
	public void testJMeterJtlFileWithSamplesOnly() throws Exception {
		String[] runArgs = {
			"--report.dir",
			temporaryFolder.getRoot().getPath(),
			"jmeter",
			"--report-logline-type",
			"sample",
			"-gt",
			"-gh",
			"-gr",
			"-gp",
			"-group-by-http-status",
			"src/test/resources/jmeter/result_with_url_and_sample_transaction_lines.jtl"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		// sampler samples (e.g. http request)
		assertTrue("Contains 3 GET Jaw Kat", result.contains("GET Jaw Kat,200,3"));
		assertTrue("Contains 11 GET Jaw Yaw", result.contains("GET Jaw Yaw,200,11"));
		// transaction controller samples
		assertFalse("Contains 3 cog", result.contains("cog,200,3"));
		assertFalse("Contains 12 som", result.contains("som,200,12"));

		assertTrue("The files contain 14 lines of which 0 failure lines.", result.contains("total-counter-success-total,14,0"));
	}

	@Test
	public void testJMeterJtlFileWithTransactionsOnly() throws Exception {
		String[] runArgs = {
			"--report.dir",
			temporaryFolder.getRoot().getPath(),
			"jmeter",
			"--report-logline-type",
			"transaction",
			"-gt",
			"-gh",
			"-gr",
			"-gp",
			"-group-by-http-status",
			"src/test/resources/jmeter/result_with_url_and_sample_transaction_lines.jtl"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		// sampler samples (e.g. http request)
		assertFalse("Contains 3 GET Jaw Kat", result.contains("GET Jaw Kat,200,3"));
		assertFalse("Contains 11 GET Jaw Yaw", result.contains("GET Jaw Yaw,200,11"));
		// transaction controller samples
		assertTrue("Contains 3 cog", result.contains("cog,200,3"));
		assertTrue("Contains 12 som", result.contains("som,200,12"));

		assertTrue("The files contain 15 lines of which 0 failure lines.", result.contains("total-counter-success-total,15,0"));
	}

	@Test
	public void testJMeterJtlFileWithSamplesAndTransactionsBug() throws Exception {

    	// seems jmeter sometimes reports transaction controller sample lines
		// without the marker in the responseMessage column: "Number of samples in transaction"

		// so also in the jMeter's own report these are reported as regular samples, which seems incorrect

		// LogRater looks at URL column if present and uses that in favour of the responseMessage check
		// if the URL is not there, the responseMessage filter is used (so the controller transactions are reported
		// as regular samples)

		String[] runArgs = {
			"--report.dir",
			temporaryFolder.getRoot().getPath(),
			"jmeter",
			"--report-logline-type",
			"sample",
			"-gt",
			"-gh",
			"-gr",
			"-gp",
			"-group-by-http-status",
			"src/test/resources/jmeter/result_with_url_and_sample_transaction_lines_with_bug.jtl"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		// sampler samples (e.g. http request)
		assertTrue("Contains 1 GET Jaw Kat", result.contains("GET Jaw Kat,200,1"));
		assertTrue("Contains 1 GET Jaw Yaw", result.contains("GET Jaw Yaw,200,1"));
		// transaction controller samples
		assertFalse("Contains 1 cog", result.contains("cog,200,1"));
		assertFalse("Contains 1 som", result.contains("som,200,1"));
		// buggy transaction controller samples
		assertFalse("Contains 3 ane transactions", result.contains("ane,204,3"));

		assertTrue("The files contain 2 lines of which 0 failure lines.", result.contains("total-counter-success-total,2,0"));
	}

	@Test
	public void testJMeterJtlFileWithTransactionsAndTransactionsBug() throws Exception {

		String[] runArgs = {
			"--report.dir",
			temporaryFolder.getRoot().getPath(),
			"jmeter",
			"--report-logline-type",
			"transaction",
			"-gt",
			"-gh",
			"-gr",
			"-gp",
			"-group-by-http-status",
			"src/test/resources/jmeter/result_with_url_and_sample_transaction_lines_with_bug.jtl"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		// sampler samples (e.g. http request)
		assertFalse("Contains 1 GET Jaw Kat", result.contains("GET Jaw Kat,200,1"));
		assertFalse("Contains 1 GET Jaw Yaw", result.contains("GET Jaw Yaw,200,1"));
		// transaction controller samples
		assertTrue("Contains 1 cog", result.contains("cog,200,1"));
		assertTrue("Contains 1 som", result.contains("som,200,1"));
		// buggy transaction controller samples, are classified as transactions because URL column is available
		assertTrue("Contains 3 ane transactions", result.contains("ane,204,3"));

		assertTrue("The files contain 5 lines of which 0 failure lines.", result.contains("total-counter-success-total,5,0"));
	}

	@Test
	public void testJMeterJtlFileWithTransactionsAndTransactionsBugNoUrl() throws Exception {

		String[] runArgs = {
			"--report.dir",
			temporaryFolder.getRoot().getPath(),
			"jmeter",
			"--report-logline-type",
			"sample",
			"-gt",
			"-gh",
			"-gr",
			"-gp",
			"-group-by-http-status",
			"src/test/resources/jmeter/result_with_url_and_sample_transaction_lines_with_bug_no_url.jtl"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		// sampler samples (e.g. http request)
		assertTrue("Contains 1 GET Jaw Kat", result.contains("GET Jaw Kat,200,1"));
		assertTrue("Contains 1 GET Jaw Yaw", result.contains("GET Jaw Yaw,200,1"));
		// transaction controller samples
		assertFalse("Contains 1 cog", result.contains("cog,200,1"));
		assertFalse("Contains 1 som", result.contains("som,200,1"));
		// buggy transaction controller samples, are classified as transactions because URL column is available
		// THIS IS ACTUALLY WRONG! Should not be samples! With 'transaction' type these are now missing!
		assertTrue("Contains 3 ane transactions", result.contains("ane,204,3"));

		// THIS IS ACTUALLY WRONG! Should be 2 total lines
		assertTrue("The files contain 5 lines of which 0 failure lines.", result.contains("total-counter-success-total,5,0"));
	}

	@Test
	public void testJMeterJtlFileWithTransactionsAndSamplesSameName() throws Exception {

		String[] runArgs = {
			"--report.dir",
			temporaryFolder.getRoot().getPath(),
			"jmeter",
			"--report-logline-type",
			"all",
			"-gt",
			"-gh",
			"-gr",
			"-gp",
			"-group-by-http-status",
			"src/test/resources/jmeter/result_with_url_and_sample_transaction_lines_with_same_names.jtl"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		// Sampler samples and transaction controllers combined because same name.
		// Seems jmeter report has same behaviour in this case.
		// Seems questionable if this is wanted: now samples and transactions are counted double: e.g. http load seems twice as high.
		// In this example the ratio transactions:samples is 1:1, can also be 1:N
		assertTrue("Contains 2 GET Jaw Kat", result.contains("GET Jaw Kat,200,2"));
		assertTrue("Contains 2 GET Jaw Yaw", result.contains("GET Jaw Yaw,200,2"));

		assertTrue("The files contain 4 lines of which 0 failure lines.", result.contains("total-counter-success-total,4,0"));
	}

	@Test
	public void testJMeterJtlFileWithMultiLines() throws Exception {

		String[] runArgs = {
			"--report.dir",
			temporaryFolder.getRoot().getPath(),
			"jmeter",
			"--report-logline-type",
			"all",
			"-gt",
			"-gh",
			"-gr",
			"-gp",
			"-group-by-http-status",
			"src/test/resources/jmeter/result_with_multi_lines.jtl"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		// Check if the multi-lines are parsed
		assertTrue("Contains 2 sample_name", result.contains("sample_name,500,2"));
		assertTrue("Contains 2 GET Jaw Kat", result.contains("GET Jaw Kat,200,2"));
		assertTrue("Contains 2 GET Jaw Yaw", result.contains("GET Jaw Yaw,200,2"));

		assertTrue("The files contain 6 lines of which 2 failure lines.", result.contains("total-counter-success-total,6,2"));
	}

	@Test
	public void testJMeterJtlFileWithMultiLinesCorrupted() throws Exception {

		String[] runArgs = {
			"--report.dir",
			temporaryFolder.getRoot().getPath(),
			"jmeter",
			"--report-logline-type",
			"all",
			"-gt",
			"-gh",
			"-gr",
			"-gp",
			"-group-by-http-status",
			"src/test/resources/jmeter/result_with_multi_lines_corrupted.jtl"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.printf("[%s]%n", result);

		// Check if the multi-lines are parsed, note: one of two is corrupt
		assertTrue("Contains 1 sample_name", result.contains("sample_name,500,1"));
		assertTrue("Contains 2 GET Jaw Kat", result.contains("GET Jaw Kat,200,2"));
		assertTrue("Contains 2 GET Jaw Yaw", result.contains("GET Jaw Yaw,200,2"));

		assertTrue("The files contain 5 lines of which 1 failure lines.", result.contains("total-counter-success-total,5,1"));
	}
}

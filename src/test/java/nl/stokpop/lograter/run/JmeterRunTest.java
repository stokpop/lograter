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
package nl.stokpop.lograter.run;

import nl.stokpop.lograter.util.LogRaterRunTestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static junit.framework.TestCase.assertTrue;

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
		System.out.println(String.format("[%s]", result));

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
		System.out.println(String.format("[%s]", result));

		assertTrue("Contains 1 error 500", result.contains("stubby1/show,500,1"));
		assertTrue("Contains 5 errors 404", result.contains("stubby3/show,404,5"));
		assertTrue("Contains 2 errors 503", result.contains("stubby3/show,503,2"));
		assertTrue("The files contain 29 lines of which 8 failure lines.", result.contains("total-counter-success-total,29,8"));

	}

}

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

import static org.junit.Assert.assertTrue;

public class IisLogRunTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testIisLogRun() throws Exception {
        String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
                "iis",
                "-nompr",
                "-lp",
                "#Fields: date time cs-method cs-uri-stem cs-uri-query s-port c-ip cs-version cs(User-Agent) cs(Cookie) cs(Referer) sc-status sc-substatus sc-win32-status sc-bytes cs-bytes time-taken",
                "src/test/resources/iis-log/iis_1.log"
        };

        String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
//	    System.out.println(result);
	    assertTrue("LogRater iis log run with outputs a duration.", result.contains("Duration"));

    }

    @Test
    public void testIisLogRunTwoFiles() throws Exception {
        String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
                "iis",
                "-nompr",
                "-lp",
                "#Fields: date time cs-method cs-uri-stem cs-uri-query s-port c-ip cs-version cs(User-Agent) cs(Cookie) cs(Referer) sc-status sc-substatus sc-win32-status sc-bytes cs-bytes time-taken",
                "src/test/resources/iis-log/iis_1.log",
                "src/test/resources/iis-log/iis_2.log"
        };

        String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
	    //System.out.println(result);
        assertTrue("LogRater iis log run with outputs a duration.", result.contains("Duration"));
    }

	@Test
	public void testIisLogRunTwoFilesAndSessionCalculator() throws Exception {
        String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
                "iis",
                "-nompr",
                "-lp",
                "#Fields: date time cs-method cs-uri-stem cs-uri-query s-port c-ip cs-version cs(User-Agent) cs(Cookie) cs(Referer) sc-status sc-substatus sc-win32-status sc-bytes cs-bytes time-taken",
                "-sessionfield-regexp",
                "fed=(.*?)(?<=;|$)",
                "-sessionfield",
                "cs(Cookie)",
                "-clickpath",
                "--clickpath-report-step-duration",
                "-session-duration",
                "src/test/resources/iis-log/iis_1.log",
                "src/test/resources/iis-log/iis_2.log"
        };

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);

		assertTrue("LogRater iis log run with outputs a duration.", result.contains("Duration"));
	}

    @Test
    public void testIisLogRunWithUnknownField() throws Exception {

        // s-event is unknown field, does parsing fail?
        String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
                "iis",
                "-nompr",
                "-lp",
                "#Fields: date time cs-method cs-uri-stem cs-uri-query s-port c-ip cs-version cs(User-Agent) cs(Cookie) cs(Referer) s-event sc-substatus sc-win32-status sc-bytes cs-bytes time-taken",
                "src/test/resources/iis-log/iis_1.log"
        };

        String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);

        assertTrue("LogRater iis log run with outputs a duration.", result.contains("Duration"));

    }
}

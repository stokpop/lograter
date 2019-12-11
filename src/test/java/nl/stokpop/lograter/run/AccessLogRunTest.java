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

public class AccessLogRunTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

	/**
	 * This used to blow up with:
	 *
	 * java.lang.IllegalArgumentException: Illegal group reference
	 *
	 * 	at java.util.regex.Matcher.appendReplacement(Matcher.java:857)
	 * 	at java.util.regex.Matcher.replaceFirst(Matcher.java:1004)
	 * 	at nl.stokpop.lograter.graphs.HtmlGraphCreator.createResponseTimesTable(HtmlGraphCreator.java:189)
	 *
	 * 	because of the $ in the url (which becomes name of counter).
	 *
	 */
    @Test
	public void testAccessLogWith$() throws Exception {
        String[] runArgs = {
                "--report.dir",
                tempFolder.getRoot().getPath(),
                //"-debug",
                "access",
                //"-imm",
                "-lp",
                "\"%{IP}i\" %V %t \"%r\" %>s %b %D \"%{SESSION}C\" \"%{host}i\" \"%{Referer}i\" \"%{User-Agent}i\"",
                "-group-by-http-status",
                "-group-by-http-method",
                //"-count-no-mapper-as-one",
                "--include-mapper-regexp-column",
                "-gt",
                "-gh",
                "-gp",
                "-gr",
                "src/test/resources/access-log-4/access.log.txt"};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.println(String.format("[%s]", result));

		assertTrue("Contains text [Duration]", result.contains("Duration"));
		assertTrue("Contains text [99%]", result.contains("99%"));
		assertTrue("The files contain 20 lines and of which 5 failure lines.", result.contains("TOTAL,TOTAL,TOTAL-success-total,,20,5"));

	}

    @Test
	public void testAccessLog() throws Exception {
        String[] runArgs = {
                "--report.dir",
                tempFolder.getRoot().getPath(),
                //"-debug",
                "access",
                // works better with mapper sections
                "--ignore-multi-and-no-matches",
//                "-failure-aware",
//                "false",
//                "-include-failed-hits-in-analysis",
//                "false",
                "-group-by-http-status",
                "-group-by-http-method",
                //"-count-no-mapper-as-one",
                "--include-mapper-regexp-column",
                "-gt",
                "-mf",
                "src/test/resources/access-log/mapper.txt",
                "src/test/resources/access-log/access.log",
                "src/test/resources/access-log/access.log.2"};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.println(String.format("[%s]", result));

		assertTrue("Contains text [Duration]", result.contains("Duration"));
		assertTrue("Contains text [99%]", result.contains("99%"));
		assertTrue("The files contain 144 lines and of which 8 failure lines.", result.contains("TOTAL,TOTAL,TOTAL-success-total,,144,8"));

	}

    @Test
	public void testAccessLogOverflow() throws Exception {
        String[] runArgs = {
                "--report.dir",
                tempFolder.getRoot().getPath(),
                "access",
                "--max-unique-counters",
                "2",
                "--ignore-multi-and-no-matches",
                "-mf",
                "src/test/resources/access-log/mapper.txt",
                "src/test/resources/access-log/access.log",
                "src/test/resources/access-log/access.log.2"};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.println(String.format("[%s]", result));

		assertTrue("Contains text [Duration]", result.contains("Duration"));
		assertTrue("Contains text [99%]", result.contains("99%"));
		assertTrue("The files contain 144 lines and of which 8 failure lines.", result.contains("TOTAL-success-total,144,8"));
		assertTrue("Contains an overflow counter with 16 hits and 8 failures.", result.contains("OVERFLOW-COUNTER,16,8"));

	}

	@Test
	public void testAccessLogToCsv() throws Exception {
        String[] runArgs = {
                "--report.dir",
                tempFolder.getRoot().getPath(),
                //"-debug",
                "accessToCsv",
                "--mapper-file",
                "src/test/resources/access-log/mapper.txt",
                "-sessionfield",
                "X-Client-IP",
                "-sessionfield-regexp",
                "(\\d.\\d.\\d)",
                "src/test/resources/access-log/access.log",
                "src/test/resources/access-log/access.log.2"};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.println(String.format("[%s]", result));

		assertTrue("Contains text: [LogRater]", result.contains("LogRater"));

	}

    @Test
    public void testAccessLog5Run() throws Exception {
        String[] runArgs = {
                "--report.dir",
                tempFolder.getRoot().getPath(),
                "access",
                //"-gh",
                "-group-by-http-method",
                "-group-by-http-status",
                "-lp",
                "%h %V %l %u %t \"%r\" %>s %b %D \"%{SESSION}C\" \"%{x-host}i\" \"%{Referer}i\" \"%{User-Agent}i\"",
                "-mf",
                "src/test/resources/access-log-5/mappers.config",
                "src/test/resources/access-log-5/access"
        };

        String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);

        assertTrue("LogRater access log run with outputs a duration.", result.contains("Duration"));

    }

}

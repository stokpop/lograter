/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.util.LogRaterRunTestUtil;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@NotThreadSafe
public class PerformanceCenterDbRunTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    @Ignore // get your own Results.db from Results.zip
    public void testPerformanceCenterDbRun() throws IOException {

        String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
                //"-st",
                //"20130904T110000",
                //"-et",
                //"20130904T120000",
                "pc",
                "-gh", "-gt", "-gr", "-gp",
                "/data/Results/Results.db" };

        String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
	    System.out.println(result);
	    assertTrue("Performance center log run with Duration in output.", result.contains("Duration"));

    }

	@Test
	@Ignore
	public void testPerformanceCenterMdbRun() throws IOException {

		String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
				//"-st",
				//"20130904T110000",
				//"-et",
				//"20130904T120000",
				"pc",
				//"-gh", "-gt", "-gr", "-gp",
				"/Users/pp/lograter/Results-test/Results.mdb" };

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.println(result);
		assertTrue("Performance center log run with Duration in output.", result.contains("Duration"));

	}
}

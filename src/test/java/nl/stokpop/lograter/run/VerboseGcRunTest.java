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

import nl.stokpop.lograter.util.LogRaterRunTestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;

public class VerboseGcRunTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testVerboseGcLogRun() throws Exception {
        String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
                "gc",
                "-st-analysis",
                "20140130T153000",
                "-et-analysis",
                "20140130T184500",
                "-st-memory-fit",
                "20140130T210000",
                "-et-memory-fit",
                "20140130T233000",
                "src/test/resources/gcverbose/gc_verbose_output_was85.log.gz" };

        String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);

        assertTrue("LogRater output should contain 'LogRater'.", result.contains("LogRater"));

    }

    @Test
    public void testVerboseGcLogRunGlobalAfterAF() throws Exception {
        String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
                "gc",
                "-st-analysis",
                "20140130T153000",
                "-et-analysis",
                "20140130T180000",
                "src/test/resources/gcverbose/no_global_found.log" };

        String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);

        assertTrue("LogRater output should contain 'LogRater'.", result.contains("LogRater"));

    }

}

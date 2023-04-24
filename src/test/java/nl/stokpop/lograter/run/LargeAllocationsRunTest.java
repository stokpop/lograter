/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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

public class LargeAllocationsRunTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testLargeAllocationsRun() throws Exception {
        String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
                "alloc",
                "src/test/resources/native-error/native_stderr.log.12-08-2014-07-00-16"};

        String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
        assertTrue("LogRater large allocations log run with outputs a 'Check out result' text.", result.contains("Check out result"));
    }
}

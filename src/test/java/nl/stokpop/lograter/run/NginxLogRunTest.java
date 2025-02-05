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
package nl.stokpop.lograter.run;

import nl.stokpop.lograter.util.LogRaterRunTestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class NginxLogRunTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    @Test
	public void testNginxLog() throws Exception {
		String[] runArgs = {
                "--report.dir",
                temporaryFolder.getRoot().getPath(),
				"access",
				"-log-type",
				"nginx",
				"-group-by-http-status",
				"-group-by-http-method",
                "-group-by-fields",
                "remote_addr,remote_user,http_user_agent",
				"-gt",
                "-mf",
                "src/test/resources/nginx-log/mapper.conf",
				"src/test/resources/nginx-log/nginx-default.log"
		};

		String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
		System.out.println(String.format("[%s]", result));

		assertTrue("Contains text [Duration]", result.contains("Duration"));
		assertFalse("Contains text [NO_MAPPER]", result.contains("NO_MAPPER"));
		assertTrue("Contains text [99%]", result.contains("99%"));
		assertTrue("The files contain 134 lines of which 12 failure lines.", result.contains("TOTAL,TOTAL,TOTAL-success-total,134,12"));

	}

}

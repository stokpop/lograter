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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@NotThreadSafe
public class AccessLogDbRunTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testAccessLogWithDbRun() throws IOException {

        String[] runArgs = {
                //"-st",
                //"20130904T110000",
                //"-et",
                //"20130904T120000",
                //"-debug",
                "--report.dir",
                tempFolder.getRoot().getPath(),
                "-storage",
                "externalsort",
                "-storage.dir",
                tempFolder.getRoot().getPath(),
                "-clear-db",
                //"-use-db",
                "access",
                "-gh", "-gt", "-gr", "-gp",
                "-gtps",
                "-lp",
                "\"%{X-Client-IP}i\" %V %t \"%r\" %>s %b %D \"%{x-host}i\" \"%{Referer}i\" \"%{User-Agent}i\"",
                "-nompr", "-urls",
                //"-Xfit",
                "-rpu",
                "src/test/resources/access-log/access.log"};

        String result = LogRaterRunTestUtil.getOutputFromLogRater(runArgs);
//	    System.out.println(result);
	    assertTrue("Access log run with outputs a duration.", result.contains("Duration"));

    }

}

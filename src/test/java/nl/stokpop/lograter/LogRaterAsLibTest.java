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
package nl.stokpop.lograter;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

public class LogRaterAsLibTest {

    public static final PrintWriter DEV_NULL_WRITER = new PrintWriter(new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
            // cat b > /dev/null
        }
    }));

    @Test
    public void lograterAsLib() throws IOException {
        LogRater logRater = new LogRater(DEV_NULL_WRITER);
        String[] args = {
                "-debug",
                "access",
                "-lp",
                "\"%{X-Client-IP}i\" %V %t \"%r\" %>s %b %D \"%{x-host}i\" \"%{Referer}i\" \"%{User-Agent}i\"",
                "src/test/resources/access-log/access.log"};
        // should not fail (fixed issue: NullPointerException on log field)
        logRater.startLogRater(args);
    }

}

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
package nl.stokpop.lograter.util;

import org.junit.Test;

import java.nio.file.NoSuchFileException;

import static org.junit.Assert.assertEquals;

public class FileUtilsTest {

    @Test(expected = NoSuchFileException.class)
    public void readFirstLineNoFile() throws Exception {
        FileUtils.readFirstLine("anyFile.xyz");
    }

    @Test()
    public void readFirstLine() throws Exception {
        String firstLine = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect";
        assertEquals(firstLine, FileUtils.readFirstLine("src/test/resources/jmeter/result_20180502_142434.jtl"));
    }

}
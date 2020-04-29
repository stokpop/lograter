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
package nl.stokpop.lograter.was;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class NativeStdErrorParserTest {

    private static final String FILE = "native-error/native_stderr.log.12-08-2014-07-00-16";

    @Test
    public void testNativeStdErrorParser() throws IOException {

        final URL resource = this.getClass().getClassLoader().getResource(FILE);
        if (resource == null) {
            throw new RuntimeException("file not found on classpath: " + FILE);
        }
        File file = new File(resource.getFile());

        final List<LargeAllocation> largeAllocationsFromFile = NativeStdErrorParser.getLargeAllocationsFromFile(file);

        Assert.assertEquals(4, largeAllocationsFromFile.size());

    }
}

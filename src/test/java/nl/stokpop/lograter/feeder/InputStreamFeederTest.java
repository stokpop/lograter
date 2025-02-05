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
package nl.stokpop.lograter.feeder;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InputStreamFeederTest {

    @Test
    public void feedInputStream() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("src/test/resources/feeder/feeder-test-file.log");
        FeedProcessor fileFeeder = new InputStreamFeeder("file1", inputStream);

        final AtomicInteger numberOfLines = new AtomicInteger(0);
        fileFeeder.feed(
            (filename, logLine) -> {
                numberOfLines.incrementAndGet();
                assertTrue(logLine.contains("header") || logLine.contains("1,2,3") || logLine.contains("4,5,6"));
            });

        assertEquals(3, numberOfLines.intValue());
    }

    @Test
    public void feedInputStreamSkipHeader() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("src/test/resources/feeder/feeder-test-file.log");
        FeedProcessor fileFeeder = new InputStreamFeeder("file1", inputStream, 1);

        final AtomicInteger numberOfLines = new AtomicInteger(0);
        fileFeeder.feed(
            (filename, logLine) -> {
                numberOfLines.incrementAndGet();
                assertTrue(logLine.contains("header") || logLine.contains("1,2,3") || logLine.contains("4,5,6"));
            });

        assertEquals(2, numberOfLines.intValue());
    }
}
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
package nl.stokpop.lograter.feeder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileFeederTest {

    @Test
    public void feedFiles() {
        FileFeeder fileFeeder = new FileFeeder();

        final List<String> files = new ArrayList<>();
        files.add("src/test/resources/feeder/feeder-test-file.log");

        fileFeeder.feedFilesAsString(files, (filename, logLine) -> assertTrue(logLine.contains("header") || logLine.contains("1,2,3") || logLine.contains("4,5,6")));

        final AtomicInteger numberOfLines = new AtomicInteger(0);
        fileFeeder.feedFilesAsString(files, (filename, logLine) -> numberOfLines.incrementAndGet() );

        assertEquals(3, numberOfLines.intValue());
    }

    @Test
    public void feedFilesSkipHeader() {
        FileFeeder fileFeeder = new FileFeeder(1);

        final List<String> files = new ArrayList<>();
        files.add("src/test/resources/feeder/feeder-test-file.log");

        fileFeeder.feedFilesAsString(files, (filename, logLine) -> assertTrue(!logLine.contains("header") || logLine.contains("1,2,3") || logLine.contains("4,5,6")));

        final AtomicInteger numberOfLines = new AtomicInteger(0);
        fileFeeder.feedFilesAsString(files, (filename, logLine) -> numberOfLines.incrementAndGet() );

        assertEquals(2, numberOfLines.intValue());
    }
}
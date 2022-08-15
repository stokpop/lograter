/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ChainedFeederTest {

    @Test
    public void testAddLogLine() {

        final List<String> results = new ArrayList<>();

        ChainedFeeder chainedFeeder = new ChainedFeeder("TestChainedFeeder") {

            @Override
            public String executeFeeder(String filename, String logLine) {
                if (logLine.contains("stop")) {
                    return null;
                }
                return logLine;
            }
        };

        Feeder finalFeeder = (filename, logLine) -> results.add(logLine);

        chainedFeeder.addFeeder(finalFeeder);

        chainedFeeder.addLogLine("TestFile", "This is a stop!");
        chainedFeeder.addLogLine("TestFile", "This is a flop!");

        assertEquals(1, results.size());
        assertFalse(results.get(0).contains("stop"));

    }
}
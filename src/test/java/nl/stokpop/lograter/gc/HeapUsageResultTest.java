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
package nl.stokpop.lograter.gc;

import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HeapUsageResultTest {

    @Test
    public void testCalculateGcOverhead() {
        List<GcLogEntry> gcLogEntries = new ArrayList<>();

        GcLogEntry.GcLogEntryBuilder entry1 = new GcLogEntry.GcLogEntryBuilder();
        entry1.setExclusiveDurationMs(100);
        entry1.setTimestamp(1100);

        GcLogEntry.GcLogEntryBuilder entry2 = new GcLogEntry.GcLogEntryBuilder();
        entry2.setExclusiveDurationMs(100);
        entry2.setTimestamp(1900);

        gcLogEntries.add(entry1.createGcLogEntry());
        gcLogEntries.add(entry2.createGcLogEntry());

        HeapUsageResult result = new HeapUsageResult("Test gcs", gcLogEntries);
        double gcOverhead = result.calculateGcOverheadPercentage(TimePeriod.createExcludingEndTime(1000, 2000));
        assertEquals((double) 200/800 * 100, gcOverhead, 0.00001d);
    }

}
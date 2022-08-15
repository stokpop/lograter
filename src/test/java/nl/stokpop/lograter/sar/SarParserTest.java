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
package nl.stokpop.lograter.sar;

import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import static junit.framework.TestCase.assertEquals;

public class SarParserTest {

    @Test
    public void testGetCpuEntriesFromFile() throws IOException {
        // note: this sar file has been reduced to only cpu and swp to save space
        final File file = getResourceAsFile("sar/sar29");

        SarLog sarLog = new SarParser().parseSarLog(file, TimePeriod.MAX_TIME_PERIOD);

        assertEquals(286, sarLog.getCpuAllEntries().size());
        assertEquals(286, sarLog.getSwapEntries().size());

    }

    public File getResourceAsFile(String path) {
        final URL resource = this.getClass().getClassLoader().getResource(path);
        if (resource == null) {
            throw new RuntimeException("cannot find file on classpath: " + path);
        }
        return new File(resource.getFile());
    }

    @Test
    public void testGetCpuEntriesFromFilePartial() throws IOException {
        final File file = getResourceAsFile("sar/sar30_partial");

        SarLog sarLog = new SarParser().parseSarLog(file, TimePeriod.MAX_TIME_PERIOD);

        assertEquals(26, sarLog.getCpuAllEntries().size());
        assertEquals(26, sarLog.getSwapEntries().size());
        assertEquals(2, sarLog.getCpuCount());

    }

    @Test
    public void testGetCpuEntriesCombined() throws IOException {
        final File file1 = getResourceAsFile("sar/sar30_partial");
        final File file2 = getResourceAsFile("sar/sar29");

        final File[] files = new File[] { file1, file2 };
        SarLog sarLog = new SarParser().parseSarLog(files, TimePeriod.MAX_TIME_PERIOD);

        assertEquals(26 + 286, sarLog.getCpuAllEntries().size());
        assertEquals(26 + 286, sarLog.getSwapEntries().size());
        assertEquals(2, sarLog.getCpuCount());

    }

    @Test
    public void testGetCpuEntriesFromOlderSarFile() throws IOException {
        final File file = getResourceAsFile("sar/sar28.gz");

        SarLog sarLog = new SarParser().parseSarLog(file, TimePeriod.MAX_TIME_PERIOD);

        assertEquals(286, sarLog.getCpuAllEntries().size());
        assertEquals(286, sarLog.getSwapEntries().size());

        assertEquals(2, sarLog.getCpuCount());

    }

    @Test
    public void testGetCpuEntriesFromSarFileWithWeirdValues() throws IOException {
        final File file = getResourceAsFile("sar/sar04_weird_values");

        SarLog sarLog = new SarParser().parseSarLog(file, TimePeriod.MAX_TIME_PERIOD);

        assertEquals(5, sarLog.getCpuAllEntries().size());
        assertEquals(0, sarLog.getSwapEntries().size());

        assertEquals(2, sarLog.getCpuCount());

    }

    @Test
    public void testDateParseCpuAMPM() throws IOException {
        String sarText =
            "Linux 2.6.32.x86_64 (server.stokpop.nl) \t01/30/15 \t_x86_64_\t(2 CPU)\n" +
            "\n" +
            "12:00:01 AM      CPU      %usr     %nice      %system   %iowait    %steal      %irq     %soft    %guest     %idle\n" +
            "12:05:02 AM      all     44.05      0.01      7.64      8.47      0.00      0.40      2.46      0.00     36.97\n";
        SarLog sarLog = new SarParser().parseSarLog(new StringReader(sarText), TimePeriod.MAX_TIME_PERIOD);
        assertEquals(1, sarLog.getCpuAllEntries().size());
   }

    @Test
    public void testDateParseSwapAMPM() throws IOException {
        String sarText =
            "Linux 2.6.32-504.el6.x86_64 (server.stokpop.nl) \t01/30/15 \t_x86_64_\t(2 CPU)\n" +
            "\n" +
            "12:00:01 PM     pswpin/s pswpout/s\n" +
            "12:05:01 PM     0.02      0.00\n";
        SarLog sarLog = new SarParser().parseSarLog(new StringReader(sarText), TimePeriod.MAX_TIME_PERIOD);
        assertEquals(1, sarLog.getSwapEntries().size());
    }

    @Test
    public void testRhel7Sar() throws IOException {
        String sarText =
            "Linux 3.10.0-327.10.1.el7.x86_64 (server.stokpop.nl) \t2016-05-11 \t_x86_64_\t(2 CPU)\n" +
                    "\n" +
                    "00:00:01        CPU      %usr     %nice      %sys   %iowait    %steal      %irq     %soft    %guest    %gnice     %idle\n" +
                    "00:05:01        all      3.43      0.10      1.27      0.29      0.00      0.00      0.02      0.00      0.00     94.89\n" +
                    "00:05:01          0      2.07      0.15      1.00      0.22      0.00      0.00      0.02      0.00      0.00     96.53\n" +
                    "00:05:01          1      4.80      0.04      1.54      0.35      0.00      0.00      0.01      0.00      0.00     93.25\n" +
                    "00:10:01        all      0.45      0.28      0.61      0.06      0.00      0.00      0.01      0.00      0.00     98.59\n" +
                    "00:10:01          0      0.45      0.41      0.71      0.06      0.00      0.00      0.00      0.00      0.00     98.36\n" +
                    "00:10:01          1      0.44      0.16      0.52      0.06      0.00      0.00      0.00      0.00      0.00     98.83\n" +
                    "00:15:01        all      0.70      0.09      0.49      0.04      0.00      0.00      0.01      0.00      0.00     98.68\n" +
                    "00:15:01          0      0.72      0.14      0.55      0.04      0.00      0.00      0.01      0.00      0.00     98.54\n" +
                    "00:15:01          1      0.68      0.04      0.43      0.04      0.00      0.00      0.01      0.00      0.00     98.82";

        SarLog sarLog = new SarParser().parseSarLog(new StringReader(sarText), TimePeriod.MAX_TIME_PERIOD);
        assertEquals(2, sarLog.getCpuCount());
        assertEquals(3, sarLog.getCpuAllEntries().size());
    }

    @Test
    public void testRhel7SarPMAM() throws IOException {
        String sarText =
                "Linux 3.10.0-327.10.1.el7.x86_64 (server.stokpop.nl) \t2016-05-11 \t_x86_64_\t(2 CPU)\n" +
                        "\n" +
                        "01:00:01 AM     CPU      %usr     %nice      %sys   %iowait    %steal      %irq     %soft    %guest    %gnice     %idle\n" +
                        "01:05:01 AM     all      3.43      0.10      1.27      0.29      0.00      0.00      0.02      0.00      0.00     94.89\n" +
                        "01:05:01 AM       0      2.07      0.15      1.00      0.22      0.00      0.00      0.02      0.00      0.00     96.53\n" +
                        "01:05:01 AM       1      4.80      0.04      1.54      0.35      0.00      0.00      0.01      0.00      0.00     93.25\n";

        SarLog sarLog = new SarParser().parseSarLog(new StringReader(sarText), TimePeriod.MAX_TIME_PERIOD);
        assertEquals(2, sarLog.getCpuCount());
        assertEquals(1, sarLog.getCpuAllEntries().size());
    }


    @Test
    public void testGetCpuEntriesEmptySarFile() throws IOException {
        final File file = getResourceAsFile("sar/sar04_weird_values");
        final File fileEmpty = getResourceAsFile("sar/sar04_empty.gz");

        SarLog sarLog = new SarParser().parseSarLog(file, TimePeriod.MAX_TIME_PERIOD);
        SarLog sarLogEmpty = new SarParser().parseSarLog(fileEmpty,TimePeriod.MAX_TIME_PERIOD);
        sarLog.add(sarLogEmpty);
        sarLogEmpty.add(sarLog);

        assertEquals(5, sarLog.getCpuAllEntries().size());
        assertEquals(0, sarLog.getSwapEntries().size());
        assertEquals(2, sarLog.getCpuCount());

        assertEquals(5, sarLogEmpty.getCpuAllEntries().size());
        assertEquals(0, sarLogEmpty.getSwapEntries().size());
        assertEquals(2, sarLogEmpty.getCpuCount());

    }

}
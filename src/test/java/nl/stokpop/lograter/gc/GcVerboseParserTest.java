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
package nl.stokpop.lograter.gc;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GcVerboseParserTest {

    private static final int MINUTE_MILLIS = 60 * 1000;

    @Test
    public void testOpenJDKgcFile() throws IOException {
    	final List<GcLogEntry> gcLogEntries = getGcLogEntries("gcverbose/gc_openjdk_1.8.log");
    	gcLogEntrySanityCheck(gcLogEntries);
    	gcLogEntrySanityCheck(gcLogEntries);
    }

    @Test
    public void testWas85WithLongGcsParserV2() throws IOException {
        // this file includes start-sys with reason=".*" attribute
        final List<GcLogEntry> gcLogEntries = getGcLogEntries("gcverbose/gc_verbose_output_sys_was85_v2.log.002.gz");
        gcLogEntrySanityCheck(gcLogEntries);

//        Concurrent collection count	0
//        Forced collection count	3
//        GC Mode	gencon
//        Global collections - Mean garbage collection pause (ms)	491
//        Global collections - Mean interval between collections (ms)	230986
//        Global collections - Number of collections	3
//        Global collections - Total amount tenured (MB)	229
//        Largest memory request (bytes)	262408
//        Number of collections triggered by allocation failure	350
//        Nursery collections - Mean garbage collection pause (ms)	65.9
//        Nursery collections - Mean interval between collections (ms)	8218
//        Nursery collections - Number of collections	350
//        Nursery collections - Total amount flipped (MB)	20618
//        Nursery collections - Total amount tenured (MB)	324
//        Proportion of time spent in garbage collection pauses (%)	0.87
//        Proportion of time spent unpaused (%)	99.13
//        Rate of garbage collection (MB/minutes)	3911


        int expectedNurseryCollections = 350;
        int expectedForcedGlobalCollections = 3;
        int expectedConcurrentCollections = 0;
        int expectedCollections = expectedNurseryCollections + expectedForcedGlobalCollections + expectedConcurrentCollections;
        Assert.assertEquals(expectedCollections, gcLogEntries.size());

        HeapUsageResult result = new HeapUsageResult(gcLogEntries);
        Assert.assertEquals(expectedForcedGlobalCollections, result.countSysGcs());
        Assert.assertEquals(expectedNurseryCollections, result.countNurseryGc());
        Assert.assertEquals(expectedConcurrentCollections, result.countConcurrentGcs());
        Assert.assertEquals(0, result.getNonSysGsWithDurationLongerThan(1000).size());
        Assert.assertEquals(1, result.getGsWithDurationLongerThan(1000).size());
        Assert.assertEquals(0.87, result.calculateGcOverheadPercentage(), 0.01);

        HeapUsageResult heapUsageResult = new HeapUsageResult(gcLogEntries);

        reportGcInfoWithFitTime(heapUsageResult, "test-run-3");

    }

    @Test
    public void testWas85WithLongGcsParser() throws IOException {

        final List<GcLogEntry> gcLogEntries = getGcLogEntries("gcverbose/gc_verbose_output_long_gc.007.gz");
        gcLogEntrySanityCheck(gcLogEntries);

//        Concurrent collection count	0
//        Forced collection count	13
//        GC Mode	gencon
//        Global collections - Mean garbage collection pause (ms)	1234
//        Global collections - Mean interval between collections (ms)	321260
//        Global collections - Number of collections	13
//        Global collections - Total amount tenured (MB)	3898
//        Largest memory request (bytes)	2099208
//        Number of collections triggered by allocation failure	339
//        Nursery collections - Mean garbage collection pause (ms)	51.9
//        Nursery collections - Mean interval between collections (ms)	11690
//        Nursery collections - Number of collections	339
//        Nursery collections - Total amount flipped (MB)	13837
//        Nursery collections - Total amount tenured (MB)	419
//        Proportion of time spent in garbage collection pauses (%)	0.86
//        Proportion of time spent unpaused (%)	99.14
//        Rate of garbage collection (MB/minutes)	2871


        int expectedNurseryCollections = 339;
        int expectedForcedGlobalCollections = 13;
        int expectedCollections = expectedNurseryCollections + expectedForcedGlobalCollections;
        Assert.assertEquals(expectedCollections, gcLogEntries.size());

        HeapUsageResult result = new HeapUsageResult(gcLogEntries);
        Assert.assertEquals(expectedForcedGlobalCollections, result.countSysGcs());
        Assert.assertEquals(expectedNurseryCollections, result.countNurseryGc());
        Assert.assertEquals(0, result.getNonSysGsWithDurationLongerThan(1000).size());
        Assert.assertEquals(10, result.getGsWithDurationLongerThan(1000).size());
        Assert.assertEquals(0.86, result.calculateGcOverheadPercentage(), 0.005);

        HeapUsageResult heapUsageResult = new HeapUsageResult(gcLogEntries);

        reportGcInfoWithFitTime(heapUsageResult, "test-run-4");

    }

    @Test
    public void testWas85WithSystemGcsParser() throws IOException {

        final List<GcLogEntry> gcLogEntries = getGcLogEntries("gcverbose/gc_verbose_output_was85.log.gz");

        Assert.assertEquals(933, gcLogEntries.size());

        gcLogEntrySanityCheck(gcLogEntries);

        HeapUsageResult heapUsageResult = new HeapUsageResult(gcLogEntries);

        reportGcInfoWithFitTime(heapUsageResult, "test-run-1");

    }

    private void reportGcInfoWithFitTime(HeapUsageResult heapUsageResult, String runId) {

        long fitStartTime = heapUsageResult.getTimePeriod().getStartTime() + TimeUnit.MINUTES.toMillis(1);
        long fitEndTime = fitStartTime + TimeUnit.MINUTES.toMillis(40);
        TimePeriod fitTimePeriod = TimePeriod.createExcludingEndTime(fitStartTime, fitEndTime);

        GcVerboseReport report = new GcVerboseReport();
        String reportAsString = report.generateReportAsString(heapUsageResult, fitTimePeriod, fitTimePeriod, runId);
        System.out.println(reportAsString);

    }

    @Test
    public void testWas85WithSystemGcsParserExt() throws IOException {

        final List<GcLogEntry> gcLogEntries = getGcLogEntries("gcverbose/gc_verbose_output.log.005.gz");

//        Concurrent collection count	25
//        Forced collection count	3
//        GC Mode	gencon
//        Global collections - Mean garbage collection pause (ms)	172
//        Global collections - Mean interval between collections (ms)	158741
//        Global collections - Number of collections	28
//        Global collections - Total amount tenured (MB)	17263
//        Largest memory request (bytes)	524304
//        Number of collections triggered by allocation failure	456
//        Nursery collections - Mean garbage collection pause (ms)	103
//        Nursery collections - Mean interval between collections (ms)	9384
//        Nursery collections - Number of collections	455
//        Nursery collections - Total amount flipped (MB)	41466
//        Nursery collections - Total amount tenured (MB)	5512
//        Proportion of time spent in garbage collection pauses (%)	1.27
//        Proportion of time spent unpaused (%)	98.73
//        Rate of garbage collection (MB/minutes)	2899


        gcLogEntrySanityCheck(gcLogEntries);

        HeapUsageResult heapUsageResult = new HeapUsageResult(gcLogEntries);

        int expectedNurseryCollections = 455;
        int expectedForcedGlobalCollections = 3;
        int expectedConcurrentCount = 25;
        int expectedCollections = expectedNurseryCollections + expectedForcedGlobalCollections + expectedConcurrentCount;
        Assert.assertEquals(expectedCollections, gcLogEntries.size());

        HeapUsageResult result = new HeapUsageResult(gcLogEntries);
        Assert.assertEquals(expectedForcedGlobalCollections, result.countSysGcs());
        Assert.assertEquals(expectedNurseryCollections, result.countNurseryGc());
        Assert.assertEquals(0, result.getNonSysGsWithDurationLongerThan(1000).size());
        Assert.assertEquals(1, result.getGsWithDurationLongerThan(1000).size());
        Assert.assertEquals(1.27, result.calculateGcOverheadPercentage(), 0.01);

        reportGcInfoWithFitTime(heapUsageResult, "test-run-2");

    }

    @Test
    public void testWas85WithSystemGcsParser2() throws IOException {

        final List<GcLogEntry> gcLogEntries = getGcLogEntries("gcverbose/gc_verbose_output_was85_with_sysgc.log.gz");

        Assert.assertEquals(114, gcLogEntries.size());

        gcLogEntrySanityCheck(gcLogEntries);

    }

    @Test
    public void testWas7GcsParser() throws IOException {

        final List<GcLogEntry> gcLogEntries = getGcLogEntries("gcverbose/gc_verbose_output.log.006.bz2");

        Assert.assertEquals(1003, gcLogEntries.size());

        gcLogEntrySanityCheck(gcLogEntries);

    }

    @Test
    public void testWas85WithCheckForNoHeapSizeEntries() throws IOException {

        final List<GcLogEntry> gcLogEntries = getGcLogEntries("gcverbose/gc_verbose_output-0.0.69-no-heap-issue.log.006.gz");

        Assert.assertEquals(353, gcLogEntries.size());

        gcLogEntrySanityCheck(gcLogEntries);

    }

	@Test
	public void testWas_8_5_5_10_Format() throws IOException {

		final List<GcLogEntry> gcLogEntries = getGcLogEntries("gcverbose/gc_verbose_output.ws8.5.5.10.log.008.gz");

		Assert.assertEquals(58, gcLogEntries.size());

		gcLogEntrySanityCheck(gcLogEntries);

	}

    private List<GcLogEntry> getGcLogEntries(String verboseGcLogFile) throws IOException {
        File file = new File(verboseGcLogFile);
        if (!file.exists()) {
            final URL resource = this.getClass().getClassLoader().getResource(verboseGcLogFile);
            if (resource == null) {
            	throw new LogRaterException("Cannot read file: " + verboseGcLogFile);
            }
            file = new File(resource.getFile());
        }
        return GcVerboseParser.getGcLogEntriesFromFile(file);
    }

    private void gcLogEntrySanityCheck(List<GcLogEntry> gcLogEntries) {
        for (GcLogEntry entry : gcLogEntries) {
            Assert.assertTrue("Used heap should not be 0 for " + entry, entry.getTotalUsedBytes() != 0);
        }
    }

}

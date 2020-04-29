package nl.stokpop.lograter.jmx.parse;

import nl.stokpop.lograter.gc.GcLogParseException;
import nl.stokpop.lograter.jmx.memory.parse.GcAlgorithmDetector;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static nl.stokpop.lograter.jmx.memory.algorithm.GcAlgorithm.*;
import static org.junit.Assert.assertEquals;

public class GcAlgorithmDetectorTest {
    @Test
    public void detectConcurrentMarkSweepTest() {
        assertEquals(CONCURRENT_MARK_SWEEP, GcAlgorithmDetector.INSTANCE.detect(getFile("jmx/jvm-heap-metrics-concurrent-mark-sweep.log")));
    }

    @Test
    public void detectG1Test() {
        assertEquals(G1, GcAlgorithmDetector.INSTANCE.detect(getFile("jmx/jvm-heap-metrics-G1.log")));
    }

    @Test
    public void detectMarkSweepCompactTest() {
        assertEquals(MARK_SWEEP_COMPACT, GcAlgorithmDetector.INSTANCE.detect(getFile("jmx/jvm-heap-metrics-mark-sweep-compact.log")));
    }

    @Test
    public void detectMarkSweepCompactJava10Test() {
        assertEquals(MARK_SWEEP_COMPACT_JAVA_10, GcAlgorithmDetector.INSTANCE.detect(getFile("jmx/jvm-heap-metrics-mark-sweep-compact-java-10.log")));
    }

    @Test
    public void detectParallelTest() {
        assertEquals(PARALLEL, GcAlgorithmDetector.INSTANCE.detect(getFile("jmx/jvm-heap-metrics-parallel.log")));
    }

    @Test(expected = GcLogParseException.class)
    public void detectNonExistingTest() {
        GcAlgorithmDetector.INSTANCE.detect(getFile("jmx/sar28"));
    }

    private File getFile(String filePath) {
        URL resource = this.getClass().getClassLoader().getResource(filePath);
        assert resource != null;
        return new File(resource.getFile());
    }
}
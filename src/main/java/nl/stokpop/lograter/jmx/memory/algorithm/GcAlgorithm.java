package nl.stokpop.lograter.jmx.memory.algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum GcAlgorithm {
    CONCURRENT_MARK_SWEEP("ParNew", "Concurrent Mark Sweep", MemoryConcurrentMarkSweep.class,
            "Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,ParNew,ConcurrentMarkSweep,Metaspace,CompressedClassSpace,CodeCache,ParEdenSpace,ParSurvivorSpace,CMSOldGen"),
    G1("G1", "G1", MemoryG1.class,
            "Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,G1YoungGeneration,G1OldGeneration,Metaspace,G1SurvivorSpace,CompressedClassSpace,G1EdenSpace,G1OldGen,CodeCache"),
    G1_JAVA_10("G1", "G1", MemoryG1Java10.class,
            "Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,G1YoungGeneration,G1OldGeneration,Metaspace,CodeHeap'non-nmethods',CodeHeap'profilednmethods',CompressedClassSpace,G1EdenSpace,G1OldGen,G1SurvivorSpace,CodeHeap'non-profilednmethods'"),
    MARK_SWEEP_COMPACT("Copy", "Mark Sweep Compact", MemoryMarkSweepCompact.class,
            "Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,Copy,MarkSweepCompact,Metaspace,TenuredGen,EdenSpace,SurvivorSpace,CompressedClassSpace,CodeCache"),
    MARK_SWEEP_COMPACT_JAVA_10("Copy", "Mark Sweep Compact", MemoryMarkSweepCompactJava10.class,
            "Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,Copy,MarkSweepCompact,Metaspace,CodeHeap'non-nmethods',TenuredGen,EdenSpace,CodeHeap'profilednmethods',SurvivorSpace,CompressedClassSpace,CodeHeap'non-profilednmethods'"),
    PARALLEL("PSMarkSweep", "Parallel Scavenge", MemoryParallelGc.class,
            "Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,PSMarkSweep,PSScavenge,Metaspace,PSOldGen,PSEdenSpace,CompressedClassSpace,CodeCache,PSSurvivorSpace");

    private String youngGenerationGcAlgorithm;
    private String oldGenerationGcAlgorithm;
    private Class bean;
    private String pattern;

    public static Optional<GcAlgorithm> forPattern(String pattern) {
        Optional<GcAlgorithm> result = Optional.empty();

        for (GcAlgorithm gcAlgorithm : values()) {
            if (gcAlgorithm.getPattern().equalsIgnoreCase(pattern)) {
                result = Optional.of(gcAlgorithm);
            }
        }

        return result;
    }
}

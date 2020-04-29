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
package nl.stokpop.lograter.jmx.memory.algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.stokpop.lograter.jmx.memory.MemoryMetrics;

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

    private final String youngGenerationGcAlgorithm;
    private final String oldGenerationGcAlgorithm;
    private final Class<? extends MemoryMetrics> beanType;
    private final String pattern;

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

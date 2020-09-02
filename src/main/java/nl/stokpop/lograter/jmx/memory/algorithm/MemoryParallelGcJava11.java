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

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import nl.stokpop.lograter.jmx.memory.MemoryMetrics;
import nl.stokpop.lograter.util.time.DateUtils;

@Data
public class MemoryParallelGcJava11 implements MemoryMetrics {
    //Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,PSMarkSweep,PSScavenge,Metaspace,CodeHeap'non-nmethods',PSOldGen,CodeHeap'profilednmethods',CompressedClassSpace,PSSurvivorSpace,PSEdenSpace,CodeHeap'non-profilednmethods'
    @CsvBindByName
    private String timestamp;
    @CsvBindByName
    private long heapMemoryUsage;
    @CsvBindByName
    private long nonHeapMemoryUsage;
    @CsvBindByName(column = "pSMarkSweep")
    private long youngGenerationGcTime;
    @CsvBindByName(column = "pSScavenge")
    private long oldGenerationGcTime;
    @CsvBindByName
    private long metaspace;
    @CsvBindByName
    private long pSOldGen;
    @CsvBindByName
    private long pSEdenSpace;
    @CsvBindByName
    private long pSSurvivorSpace;
    @CsvBindByName
    private long compressedClassSpace;
    @CsvBindByName(column = "CodeHeap'non-nmethods'")
    private long codeCacheNonMethods;
    @CsvBindByName(column = "CodeHeap'profilednmethods'")
    private long codeCacheProfiledMethods;
    @CsvBindByName(column = "CodeHeap'non-profilednmethods'")
    private long codeCacheNonProfiledMethods;

    @Override
    public long getTimestamp() {
        return DateUtils.parseISOTime(timestamp);
    }

    @Override
    public long getHeapMemoryUsedBytes() {
        return heapMemoryUsage;
    }

    @Override
    public long getEdenUsedBytes() {
        return pSEdenSpace;
    }

    @Override
    public long getSurvivorUsedBytes() {
        return pSSurvivorSpace;
    }

    @Override
    public long getTenuredUsedBytes() {
        return pSOldGen;
    }

    @Override
    public long getOldGenerationUsedBytes() {
        return pSOldGen;
    }

    @Override
    public long getMetaSpaceUsedBytes() {
        return metaspace;
    }

    @Override
    public long getCompressedClassSpaceUsedBytes() {
        return compressedClassSpace;
    }

    @Override
    public long getCodeCacheUsedBytes() {
        return codeCacheNonMethods + codeCacheNonProfiledMethods + codeCacheProfiledMethods;
    }

    @Override
    public double getGcDurationMs() {
        return getYoungGenerationGcTime() + getOldGenerationGcTime();
    }
}

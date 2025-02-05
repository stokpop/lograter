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
package nl.stokpop.lograter.jmx.memory.algorithm;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import nl.stokpop.lograter.jmx.memory.MemoryMetrics;
import nl.stokpop.lograter.util.time.DateUtils;

@Data
public class MemoryG1 implements MemoryMetrics {
    //Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,G1YoungGeneration,G1OldGeneration,Metaspace,G1SurvivorSpace,CompressedClassSpace,G1EdenSpace,G1OldGen,CodeCache
    @CsvBindByName
    private String timestamp; //yyyy-MM-dd HH:mm:ss.SSS e.g. 2019-10-22T11:43:40.590
    @CsvBindByName
    private long heapMemoryUsage;
    @CsvBindByName
    private long nonHeapMemoryUsage;
    @CsvBindByName(column = "g1YoungGeneration")
    private long youngGenerationGcTime;
    @CsvBindByName(column = "g1OldGeneration")
    private long oldGenerationGcTime;
    @CsvBindByName
    private long metaspace;
    @CsvBindByName
    private long g1SurvivorSpace;
    @CsvBindByName
    private long compressedClassSpace;
    @CsvBindByName
    private long g1EdenSpace;
    @CsvBindByName
    private long g1OldGen;
    @CsvBindByName
    private long codeCache;

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
        return g1EdenSpace;
    }

    @Override
    public long getSurvivorUsedBytes() {
        return g1SurvivorSpace;
    }

    @Override
    public long getTenuredUsedBytes() {
        return g1OldGen;
    }

    @Override
    public long getOldGenerationUsedBytes() {
        return g1OldGen;
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
        return codeCache;
    }

    @Override
    public double getGcDurationMs() {
        return getYoungGenerationGcTime() + getOldGenerationGcTime();
    }
}
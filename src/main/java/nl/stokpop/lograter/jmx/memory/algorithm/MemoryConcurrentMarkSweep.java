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
public class MemoryConcurrentMarkSweep implements MemoryMetrics {
    //Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,ParNew,ConcurrentMarkSweep,Metaspace,CompressedClassSpace,CodeCache,ParEdenSpace,ParSurvivorSpace,CMSOldGen
    @CsvBindByName
    private String timestamp;
    @CsvBindByName
    private long heapMemoryUsage;
    @CsvBindByName
    private long nonHeapMemoryUsage;
    @CsvBindByName(column = "parNew")
    private long youngGenerationGcTime;
    @CsvBindByName(column = "concurrentMarkSweep")
    private long oldGenerationGcTime;
    @CsvBindByName
    private long metaspace;
    @CsvBindByName
    private long compressedClassSpace;
    @CsvBindByName
    private long codeCache;
    @CsvBindByName
    private long parEdenSpace;
    @CsvBindByName
    private long parSurvivorSpace;
    @CsvBindByName
    private long cMSOldGen;

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
        return parEdenSpace;
    }

    @Override
    public long getSurvivorUsedBytes() {
        return parSurvivorSpace;
    }

    @Override
    public long getTenuredUsedBytes() {
        return cMSOldGen;
    }

    @Override
    public long getOldGenerationUsedBytes() {
        return cMSOldGen;
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

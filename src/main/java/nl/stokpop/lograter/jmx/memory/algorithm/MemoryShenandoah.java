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
package nl.stokpop.lograter.jmx.memory.algorithm;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import nl.stokpop.lograter.jmx.memory.MemoryMetrics;
import nl.stokpop.lograter.util.time.DateUtils;

@Data
public class MemoryShenandoah implements MemoryMetrics {

    // The header for Shenandoah JMX Bean, listing MemoryPool names:
    //Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,ShenandoahPauses,ShenandoahCycles,Metaspace,CodeHeap'non-nmethods',CodeHeap'profilednmethods',CompressedClassSpace,CodeHeap'non-profilednmethods',Shenandoah
    @CsvBindByName
    private String timestamp; //yyyy-MM-dd HH:mm:ss.SSS e.g. 2019-10-22T11:43:40.590
    @CsvBindByName // there is heapMemoryUsage and there is the last column: Shenandoah memory pool. They seem alike, small diff, might be timing differences?
    private long heapMemoryUsage;
    @CsvBindByName
    private long nonHeapMemoryUsage;
    //@CsvBindByName(column = "ShenandoahPauses")
    private long youngGenerationGcTime;
    @CsvBindByName(column = "ShenandoahPauses") // there is no young in Shenandoah, so put all on oldGeneration
    private long oldGenerationGcTime;
    @CsvBindByName
    private long metaspace;
    @CsvBindByName
    private long compressedClassSpace;
    @CsvBindByName
    private long shenandoahCycles;

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
        return 0; // no specific eden in Shenandoah
    }

    @Override
    public long getSurvivorUsedBytes() {
        return 0; // no specific survivor in Shenandoah
    }

    @Override
    public long getTenuredUsedBytes() {
        return 0; // no specific tenured in Shenandoah
    }

    @Override
    public long getOldGenerationUsedBytes() {
        return 0; // no specific old gen bytes in Shenandoah
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
        return 0; // could this be: CodeHeap'non-nmethods' + CodeHeap'profilednmethods' + CodeHeap'non-profilednmethods'?
    }

    @Override
    public double getGcDurationMs() {
        return ((double) getYoungGenerationGcTime()) + getOldGenerationGcTime();
    }

    // No override, only available when cast to MemoryShenandoah class
    public long getShenandoahCycles() {
        return shenandoahCycles;
    }
}
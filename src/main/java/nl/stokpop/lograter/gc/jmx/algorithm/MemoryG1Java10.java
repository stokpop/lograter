package nl.stokpop.lograter.gc.jmx.algorithm;

import nl.stokpop.lograter.gc.jmx.MemoryMetrics;
import nl.stokpop.lograter.util.time.DateUtils;
import com.opencsv.bean.CsvBindByName;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MemoryG1Java10 implements MemoryMetrics {
    //Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,G1YoungGeneration,G1OldGeneration,Metaspace,CodeHeap'non-nmethods',CodeHeap'profilednmethods',CompressedClassSpace,G1EdenSpace,G1OldGen,G1SurvivorSpace,CodeHeap'non-profilednmethods'
    @CsvBindByName
    private String timestamp;
    @CsvBindByName
    private long heapMemoryUsage;
    @CsvBindByName
    private long nonHeapMemoryUsage;
    @CsvBindByName(column = "G1YoungGeneration")
    private long youngGenerationGcTime;
    @CsvBindByName(column = "G1OldGeneration")
    private long oldGenerationGcTime;
    @CsvBindByName
    private long metaspace;
    @CsvBindByName
    private long tenuredGen;
    @CsvBindByName
    private long edenSpace;
    @CsvBindByName
    private long survivorSpace;
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
        return edenSpace;
    }

    @Override
    public long getSurvivorUsedBytes() {
        return survivorSpace;
    }

    @Override
    public long getTenuredUsedBytes() {
        return tenuredGen;
    }

    @Override
    public long getOldGenerationUsedBytes() {
        return tenuredGen;
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

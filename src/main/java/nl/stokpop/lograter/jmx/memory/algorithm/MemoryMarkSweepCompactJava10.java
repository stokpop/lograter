package nl.stokpop.lograter.jmx.memory.algorithm;

import com.opencsv.bean.CsvBindByName;
import lombok.*;
import nl.stokpop.lograter.jmx.memory.MemoryMetrics;
import nl.stokpop.lograter.util.time.DateUtils;

@Data
public class MemoryMarkSweepCompactJava10 implements MemoryMetrics {
    //Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,Copy,MarkSweepCompact,Metaspace,CodeHeap'non-nmethods',TenuredGen,EdenSpace,CodeHeap'profilednmethods',SurvivorSpace,CompressedClassSpace,CodeHeap'non-profilednmethods'
    @CsvBindByName
    private String timestamp;
    @CsvBindByName
    private long heapMemoryUsage;
    @CsvBindByName
    private long nonHeapMemoryUsage;
    @CsvBindByName(column = "copy")
    private long youngGenerationGcTime;
    @CsvBindByName(column = "markSweepCompact")
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

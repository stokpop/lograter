package nl.stokpop.lograter.gc.jmx.algorithm;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.stokpop.lograter.gc.jmx.GcMetrics;
import nl.stokpop.lograter.util.time.DateUtils;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MemoryMarkSweepCompact implements GcMetrics {
    //Timestamp,HeapMemoryUsage,NonHeapMemoryUsage,Copy,MarkSweepCompact,Metaspace,TenuredGen,EdenSpace,SurvivorSpace,CompressedClassSpace,CodeCache
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
    public long getOldGenerationUsedBytes() {
        return tenuredGen;
    }

    @Override
    public double getGcDurationMs() {
        return getYoungGenerationGcTime() + getOldGenerationGcTime();
    }
}
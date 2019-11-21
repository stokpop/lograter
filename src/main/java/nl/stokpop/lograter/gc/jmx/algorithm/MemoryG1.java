package nl.stokpop.lograter.gc.jmx.algorithm;

import com.opencsv.bean.CsvBindByName;
import lombok.*;
import nl.stokpop.lograter.gc.jmx.GcMetrics;
import nl.stokpop.lograter.util.time.DateUtils;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MemoryG1 implements GcMetrics {
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
    public long getOldGenerationUsedBytes() {
        return g1OldGen;
    }

    @Override
    public double getGcDurationMs() {
        return getYoungGenerationGcTime() + getOldGenerationGcTime();
    }
}
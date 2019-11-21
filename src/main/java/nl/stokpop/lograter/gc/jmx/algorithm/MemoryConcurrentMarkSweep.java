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
public class MemoryConcurrentMarkSweep implements GcMetrics {
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
    public long getOldGenerationUsedBytes() {
        return cMSOldGen;
    }

    @Override
    public double getGcDurationMs() {
        return getYoungGenerationGcTime() + getOldGenerationGcTime();
    }
}

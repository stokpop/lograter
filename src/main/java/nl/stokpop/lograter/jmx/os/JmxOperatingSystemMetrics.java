package nl.stokpop.lograter.jmx.os;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.stokpop.lograter.util.time.DateUtils;

/*
Properties on UnixOperatingSystemMXBean. See:
https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/UnixOperatingSystemMXBean.html

Timestamp,
ProcessCpuLoad,
SystemCpuLoad,
ProcessCpuTime,
CommittedVirtualMemorySize,
TotalSwapSpaceSize,
FreeSwapSpaceSize,
FreePhysicalMemorySize,
TotalPhysicalMemorySize,
SystemLoadAverage,
AvailableProcessors,
OpenFileDescriptorCount,
MaxFileDescriptorCount
*/

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class JmxOperatingSystemMetrics {

    @CsvBindByName
    private String timestamp;
    @CsvBindByName
    private double processCpuLoad;
    @CsvBindByName
    private double systemCpuLoad;
    @CsvBindByName
    private long processCpuTime;
    @CsvBindByName
    private long committedVirtualMemorySize;
    @CsvBindByName
    private long totalSwapSpaceSize;
    @CsvBindByName
    private long freeSwapSpaceSize;
    @CsvBindByName
    private long freePhysicalMemorySize;
    @CsvBindByName
    private long totalPhysicalMemorySize;
    @CsvBindByName
    private double systemLoadAverage;
    @CsvBindByName
    private long availableProcessors;
    @CsvBindByName
    private long openFileDescriptorCount;
    @CsvBindByName
    private long maxFileDescriptorCount;

    public long getTimestamp() {
        return DateUtils.parseISOTime(timestamp);
    }

    public double getProcessCpuLoad() {
        return processCpuLoad;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public long getProcessCpuTime() {
        return processCpuTime;
    }

    public long getCommittedVirtualMemorySize() {
        return committedVirtualMemorySize;
    }

    public long getTotalSwapSpaceSize() {
        return totalSwapSpaceSize;
    }

    public long getFreeSwapSpaceSize() {
        return freeSwapSpaceSize;
    }

    public long getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }

    public long getTotalPhysicalMemorySize() {
        return totalPhysicalMemorySize;
    }

    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public long getAvailableProcessors() {
        return availableProcessors;
    }

    public long getOpenFileDescriptorCount() {
        return openFileDescriptorCount;
    }

    public long getMaxFileDescriptorCount() {
        return maxFileDescriptorCount;
    }
}

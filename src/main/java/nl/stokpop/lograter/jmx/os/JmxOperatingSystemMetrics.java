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
package nl.stokpop.lograter.jmx.os;

import com.opencsv.bean.CsvBindByName;
import lombok.*;
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
@Setter
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

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setProcessCpuLoad(double processCpuLoad) {
        this.processCpuLoad = processCpuLoad;
    }

    public void setSystemCpuLoad(double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    public void setProcessCpuTime(long processCpuTime) {
        this.processCpuTime = processCpuTime;
    }

    public void setCommittedVirtualMemorySize(long committedVirtualMemorySize) {
        this.committedVirtualMemorySize = committedVirtualMemorySize;
    }

    public void setTotalSwapSpaceSize(long totalSwapSpaceSize) {
        this.totalSwapSpaceSize = totalSwapSpaceSize;
    }

    public void setFreeSwapSpaceSize(long freeSwapSpaceSize) {
        this.freeSwapSpaceSize = freeSwapSpaceSize;
    }

    public void setFreePhysicalMemorySize(long freePhysicalMemorySize) {
        this.freePhysicalMemorySize = freePhysicalMemorySize;
    }

    public void setTotalPhysicalMemorySize(long totalPhysicalMemorySize) {
        this.totalPhysicalMemorySize = totalPhysicalMemorySize;
    }

    public void setSystemLoadAverage(double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    public void setAvailableProcessors(long availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    public void setOpenFileDescriptorCount(long openFileDescriptorCount) {
        this.openFileDescriptorCount = openFileDescriptorCount;
    }

    public void setMaxFileDescriptorCount(long maxFileDescriptorCount) {
        this.maxFileDescriptorCount = maxFileDescriptorCount;
    }
}

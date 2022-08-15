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
package nl.stokpop.lograter.sar;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.sar.entry.SarCpuEntry;
import nl.stokpop.lograter.sar.entry.SarEntry;
import nl.stokpop.lograter.sar.entry.SarSwapEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Hold the info about sar log.
 */
public class SarLog {

    private List<SarCpuEntry> cpuAllEntries = new ArrayList<>();
    private List<SarSwapEntry> swapEntries = new ArrayList<>();

    private int cpuCount = -1;

    public void addCpuAllEntry(SarCpuEntry entry) {
        cpuAllEntries.add(entry);
    }

    /**
     * @return new list of all CpuAll entries, sorted on timestamp
     */
    public List<SarCpuEntry> getCpuAllEntries() {
        List<SarCpuEntry> sarCpuEntries = new ArrayList<>(cpuAllEntries);
        sortOnTimestamp(sarCpuEntries);
        return sarCpuEntries;
    }

    private void sortOnTimestamp(List<? extends SarEntry> sarEntries) {
        sarEntries.sort(Comparator.comparingLong(SarEntry::getTimestamp));
    }

    /**
     * Add all entries from the given sarLog to this one. Does a sanity check on cpu count, if these are not the same
     * then an LogRaterException is thrown. Returns this SarLog.
     */
    public SarLog add(SarLog sarLog) throws LogRaterException {
        if (sarLog.isEmpty()) {
            return this;
        }

        if (getCpuCount() == -1) {
            this.cpuCount = sarLog.getCpuCount();
        }

        if (getCpuCount() != sarLog.getCpuCount()) {
            throw new LogRaterException(String.format("Sar log files with different number of CPUs found: %d and %d, cannot merge.", getCpuCount(), sarLog.getCpuCount()));
        }

        cpuAllEntries.addAll(sarLog.getCpuAllEntries());
        sortOnTimestamp(cpuAllEntries);

        swapEntries.addAll(sarLog.getSwapEntries());
        sortOnTimestamp(swapEntries);

        return this;
    }

    private boolean isEmpty() {
        return this.cpuAllEntries.isEmpty() && this.swapEntries.isEmpty();
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public void addSwapEntry(SarSwapEntry entry) {
        this.swapEntries.add(entry);
    }

    /**
     * @return new list of all CpuAll entries, sorted on timestamp
     */
    public List<SarSwapEntry> getSwapEntries() {
        List<SarSwapEntry> sarSwapEntries = new ArrayList<>(swapEntries);
        sortOnTimestamp(sarSwapEntries);
        return sarSwapEntries;
    }

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

}

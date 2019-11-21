/*
 * Copyright (C) 2019 Peter Paul Bakker, Stokpop Software Solutions
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
package nl.stokpop.lograter.processor;

import nl.stokpop.lograter.command.BaseUnit;
import nl.stokpop.lograter.counter.CounterStorageType;

/**
 * Sets defaults for Basic analysis to:
 *
 * <ul>
 *      <li>failureAwareAnalysis to true</li>
 *      <li>includeFailedHitsInAnalysis to true</li>
 * </ul>
 *
 * Note these values can be reset after initialization.
 */
public abstract class BasicCounterLogConfig extends BasicLogConfig {

	private boolean calculateStdDev = false;
	private boolean calculateHitsPerSecond = false;
	private boolean calculateConcurrentCalls = false;
	private boolean calculateStubDelays = false;
	private CounterStorageType counterStorage = CounterStorageType.Memory;
	private boolean includeMapperRegexpColumn = false;
    private Double[] reportPercentiles = { 99d };
    private int maxNoMapperCount = 512;
    private BaseUnit baseUnit = BaseUnit.milliseconds;
    private String counterStorageDir = ".";
    private boolean failureAwareAnalysis = true;
    private boolean includeFailedHitsInAnalysis = true;

    public boolean isCalculateStdDev() {
		return calculateStdDev;
	}
	public void setCalculateStdDev(boolean calculateStdDev) {
		this.calculateStdDev = calculateStdDev;
	}
	public boolean isCalculateHitsPerSecond() {
		return calculateHitsPerSecond;
	}
	public void setCalculateHitsPerSecond(boolean calculateHitsPerSecond) {
		this.calculateHitsPerSecond = calculateHitsPerSecond;
	}
	public boolean isCalculateConcurrentCalls() {
		return calculateConcurrentCalls;
	}
	public void setCalculateConcurrentCalls(boolean calculateConcurrentCalls) {
		this.calculateConcurrentCalls = calculateConcurrentCalls;
	}

    public boolean isCalculateStubDelays() {
        return calculateStubDelays;
    }

    public void setCalculateStubDelays(boolean calculateStubDelays) {
        this.calculateStubDelays = calculateStubDelays;
    }
    
	public void setCounterStorage(CounterStorageType counterStorage) {
		this.counterStorage = counterStorage;
	}

	public CounterStorageType getCounterStorage() {
		return counterStorage;
	}

    public void setIncludeMapperRegexpColumn(boolean includeMapperRegexpColumn) {
        this.includeMapperRegexpColumn = includeMapperRegexpColumn;
    }

    public boolean isIncludeMapperRegexpColumn() {
        return includeMapperRegexpColumn;
    }

	public boolean isFailureAwareAnalysis() {
		return failureAwareAnalysis;
	}

	/**
	 * When failure counters are available, report on failure metrics in the reports.
	 * @param failureAwareAnalysis true when failures are part of the analysis,
     *                                null to use default per module 
	 */
	public void setFailureAwareAnalysis(boolean failureAwareAnalysis) {
		this.failureAwareAnalysis = failureAwareAnalysis;
	}

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

    public Double[] getReportPercentiles() {
        return reportPercentiles;
    }

    public void setReportPercentiles(Double[] reportPercentiles) {
        this.reportPercentiles = reportPercentiles;
    }
    
    public int getMaxNoMapperCount() {
        return maxNoMapperCount;
    }

    public void setMaxNoMapperCount(int maxNoMapperCount) {
        this.maxNoMapperCount = maxNoMapperCount;
    }

    public BaseUnit getBaseUnit() {
        return baseUnit;
    }

    public void setBaseUnit(BaseUnit baseUnit) {
        this.baseUnit = baseUnit;
    }

    public void setCounterStorageDir(String counterStorageDir) {
        this.counterStorageDir = counterStorageDir;
    }

    public String getCounterStorageDir() {
        return counterStorageDir;
    }

    /**
     * When FailureAwareAnalysis is true, this setting determines if failed hits should
     * be included in the analysis. If false, failure hits and failure percentage are reported,
     * but are not included in the analysis, such as determining avg and max values.
     * @param includeFailedHitsInAnalysis true or false, or null when defaults of reports should be used
     */
    public void setIncludeFailedHitsInAnalysis(boolean includeFailedHitsInAnalysis) {
        this.includeFailedHitsInAnalysis = includeFailedHitsInAnalysis;
    }

    public boolean isIncludeFailedHitsInAnalysis() {
        return includeFailedHitsInAnalysis;
    }
}
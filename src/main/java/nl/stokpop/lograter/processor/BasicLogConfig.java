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
package nl.stokpop.lograter.processor;

import nl.stokpop.lograter.util.time.TimePeriod;

public abstract class BasicLogConfig {

	private String fileFeederFilterIncludes;
	private String fileFeederFilterExcludes;
	private String runId;

	private TimePeriod filterPeriod = TimePeriod.UNDEFINED_PERIOD;

	public void setFileFeederFilterIncludes(String fileFeederFilterIncludes) {
		this.fileFeederFilterIncludes = fileFeederFilterIncludes;
	}

	public String getFileFeederFilterIncludes() {
		return fileFeederFilterIncludes;
	}

	public void setFileFeederFilterExcludes(String fileFeederFilterExcludes) {
		this.fileFeederFilterExcludes = fileFeederFilterExcludes;
	}

	public String getFileFeederFilterExcludes() {
		return fileFeederFilterExcludes;
	}

	public TimePeriod getFilterPeriod() {
		return filterPeriod;
	}

	public void setFilterPeriod(TimePeriod filterPeriod) {
		this.filterPeriod = filterPeriod;
	}

    public void setRunId(String runId) {
        this.runId = runId;
    }

	public String getRunId() {
		return runId;
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException();
	}

    @Override
    public String toString() {
        return "BasicLogConfig{" +
                "fileFeederFilterIncludes='" + fileFeederFilterIncludes + '\'' +
                ", fileFeederFilterExcludes='" + fileFeederFilterExcludes + '\'' +
                ", runId='" + runId + '\'' +
                ", filterPeriod=" + filterPeriod +
                '}';
    }
}
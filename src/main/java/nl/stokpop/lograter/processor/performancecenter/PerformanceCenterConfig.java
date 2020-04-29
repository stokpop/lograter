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
package nl.stokpop.lograter.processor.performancecenter;

import nl.stokpop.lograter.processor.BasicCounterLogConfig;

public class PerformanceCenterConfig extends BasicCounterLogConfig {
	
	private String counterFields;
	private String mapperFile;

    /**
     * Sets defaults for PerformanceCenter analysis to:
     *
     * <ul>
     *      <li>failureAwareAnalysis to true</li>
     *      <li>includeFailedHitsInAnalysis to false</li>
     * </ul>
     *
     * Note these values can be reset after initialization.
     */
	public PerformanceCenterConfig() {
	    setFailureAwareAnalysis(true);
	    setIncludeFailedHitsInAnalysis(false);
    }
	public String getCounterFields() {
		return counterFields;
	}

	public void setCounterFields(String counterFields) {
		this.counterFields = counterFields;
	}

	public String getMapperFile() {
		return mapperFile;
	}

	public void setMapperFile(String mapperFile) {
		this.mapperFile = mapperFile;
	}

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

    @Override
    public String toString() {
        return "PerformanceCenterConfig{" +
                "counterFields='" + counterFields + '\'' +
                ", mapperFile='" + mapperFile + '\'' +
                '}';
    }
}

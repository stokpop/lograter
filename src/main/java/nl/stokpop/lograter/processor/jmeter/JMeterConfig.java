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
package nl.stokpop.lograter.processor.jmeter;

import nl.stokpop.lograter.processor.BasicCounterLogConfig;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;

import java.util.Collections;
import java.util.List;

public class JMeterConfig extends BasicCounterLogConfig {
	private boolean ignoreMultiAndNoMatches = true;
	private boolean doCountMultipleMapperHits = false;
	private boolean doFilterOnHttpStatus = false;
	private List<LineMapperSection> mappers = Collections.emptyList();
    private boolean countNoMappersAsOne = false;
    private List<String> groupByFields = Collections.emptyList();
    private String logPattern = null;

    /**
     * Sets defaults for PerformanceCenter analysis to:
     *
     * <ul>
     *      <li>failureAwareAnalysis to true</li>
     *      <li>includeFailedHitsInAnalysis to true</li>
     * </ul>
     *
     * Note these values can be reset after initialization.
     */
    public JMeterConfig() {
        setFailureAwareAnalysis(true);
        setIncludeFailedHitsInAnalysis(true);
    }

	public boolean ignoreMultiAndNoMatches() {
		return ignoreMultiAndNoMatches;
	}

	public void setIgnoreMultiAndNoMatches(
			boolean ignoreMultiAndNoMatches) {
		this.ignoreMultiAndNoMatches = ignoreMultiAndNoMatches;
	}

	public boolean countMultipleMapperHits() {
		return doCountMultipleMapperHits;
	}

	public void setDoCountMultipleMapperHits(
			boolean doCountMultipleMapperHits) {
		this.doCountMultipleMapperHits = doCountMultipleMapperHits;
	}

	public boolean groupByHttpStatus() {
		return doFilterOnHttpStatus;
	}

	public void setDoFilterOnHttpStatus(boolean doFilterOnHttpStatus) {
		this.doFilterOnHttpStatus = doFilterOnHttpStatus;
	}

    public boolean countNoMappersAsOne() {
        return this.countNoMappersAsOne;
    }

    public void setCountNoMappersAsOne(boolean countNoMappersAsOne) {
        this.countNoMappersAsOne = countNoMappersAsOne;
    }

	public List<LineMapperSection> getLineMappers() {
		return mappers;
	}

	public void setLineMappers(List<LineMapperSection> mappers) {
		this.mappers = mappers;
	}

    public List<String> getGroupByFields() {
        return groupByFields;
    }

    public void setGroupByFields(List<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    public String getLogPattern() {
        return logPattern;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JMeterLogConfig{");
        sb.append("ignoreMultiAndNoMatches=").append(ignoreMultiAndNoMatches);
        sb.append(", doCountMultipleMapperHits=").append(doCountMultipleMapperHits);
        sb.append(", doFilterOnHttpStatus=").append(doFilterOnHttpStatus);
        sb.append(", mappers=").append(mappers);
        sb.append(", countNoMappersAsOne=").append(countNoMappersAsOne);
        sb.append(", groupByFields=").append(groupByFields);
        sb.append(", logPattern=").append(logPattern);
        sb.append('}');
        return sb.toString();
    }
}
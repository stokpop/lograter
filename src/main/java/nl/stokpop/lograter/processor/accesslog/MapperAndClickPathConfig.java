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
package nl.stokpop.lograter.processor.accesslog;

import nl.stokpop.lograter.processor.BasicCounterLogConfig;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;

import java.util.Collections;
import java.util.List;

public class MapperAndClickPathConfig extends BasicCounterLogConfig {
    protected int clickPathShortCodeLength;
    private boolean ignoreMultiAndNoMatches = true;
    private boolean doCountMultipleMapperHits = false;
    private List<LineMapperSection> mappers = Collections.emptyList();
    private boolean countNoMappersAsOne = false;
    private boolean clickpathReportStepDurations = false;
    private boolean determineClickpaths = false;
    private boolean determineSessionDuration = false;
    private String sessionField;
    private String sessionFieldRegexp;
    private String clickpathEndOfSessionSnippet;

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

    public boolean countNoMappersAsOne() {
        return this.countNoMappersAsOne;
    }

    public void setCountNoMappersAsOne(boolean countNoMappersAsOne) {
        this.countNoMappersAsOne = countNoMappersAsOne;
    }

    public void setClickpathReportStepDurations(boolean clickpathReportStepDurations) {
		this.clickpathReportStepDurations = clickpathReportStepDurations;
	}

    public boolean isClickpathReportStepDurations() {
		return clickpathReportStepDurations;
	}

    public void setDetermineClickpaths(boolean determineClickpaths) {
        this.determineClickpaths = determineClickpaths;
    }

    public boolean isDetermineClickpathsEnabled() {
        return determineClickpaths;
    }

    public List<LineMapperSection> getLineMappers() {
        return mappers;
    }

    public void setLineMappers(List<LineMapperSection> mappers) {
        this.mappers = mappers;
    }

    public boolean isDetermineSessionDurationEnabled() {
        return this.determineSessionDuration;
    }

    public void setDetermineSessionDuration(boolean determineSessionDuration) {
        this.determineSessionDuration = determineSessionDuration;
    }

    public void setSessionField(String sessionField) {
        this.sessionField = sessionField;
    }

    public String getSessionField() {
        return sessionField;
    }

    public void setSessionFieldRegexp(String sessionFieldRegexp) {
        this.sessionFieldRegexp = sessionFieldRegexp;
    }

    public String getSessionFieldRegexp() {
        return sessionFieldRegexp;
    }

    public void setClickpathEndOfSessionSnippet(String clickpathEndOfSessionSnippet) {
        this.clickpathEndOfSessionSnippet = clickpathEndOfSessionSnippet;
    }

    public String getClickpathEndOfSessionSnippet() {
        return clickpathEndOfSessionSnippet;
    }

    public int getClickPathShortCodeLength() {
        return clickPathShortCodeLength;
    }

    public void setClickPathShortCodeLength(int clickPathShortCodeLength) {
        this.clickPathShortCodeLength = clickPathShortCodeLength;
    }
}

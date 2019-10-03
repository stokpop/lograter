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
package nl.stokpop.lograter.processor.accesslog;

import nl.stokpop.lograter.command.CommandAccessLog;
import nl.stokpop.lograter.processor.BasicCounterLogConfig;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;

import java.util.Collections;
import java.util.List;

public class AccessLogConfig extends BasicCounterLogConfig {
	private boolean showBasicUrls = false;
	private boolean showUserAgents = false;
	private boolean showReferers = false;
	private boolean ignoreMultiAndNoMatches = true;
	private boolean doCountMultipleMapperHits = false;
	private boolean excludeMappersInIisAndAccessLogs = false;
	private boolean doFilterOnHttpMethod = false;
	private boolean doFilterOnHttpStatus = false;
	private List<LineMapperSection> mappers = Collections.emptyList();
    private boolean countNoMappersAsOne = false;
	private boolean clickpathReportStepDurations = false;
	private boolean removeParametersFromUrl = false;
	private String logPattern = null;
	private boolean determineClickpaths = false;
	private boolean determineSessionDuration = false;
	private String sessionField;
	private String sessionFieldRegexp;
	private CommandAccessLog.LogType logType = CommandAccessLog.LogType.apache;
    private List<String> groupByFields = Collections.emptyList();
    private String clickpathEndOfSessionSnippet;

    public boolean isShowBasicUrls() {
		return showBasicUrls;
	}

	public void setShowBasicUrls(boolean showBasicUrls) {
		this.showBasicUrls = showBasicUrls;
	}

	public boolean isShowUserAgents() {
		return showUserAgents;
	}

	public void setShowUserAgents(boolean showUserAgents) {
		this.showUserAgents = showUserAgents;
	}

	public boolean isShowReferers() {
		return showReferers;
	}

	public void setShowReferers(boolean showReferers) {
		this.showReferers = showReferers;
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

	public boolean isExcludeMappersInIisAndAccessLogs() {
		return excludeMappersInIisAndAccessLogs;
	}

	public void setExcludeMappersInIisAndAccessLogs(
			boolean excludeMappersInIisAndAccessLogs) {
		this.excludeMappersInIisAndAccessLogs = excludeMappersInIisAndAccessLogs;
	}

	public boolean groupByHttpMethod() {
		return doFilterOnHttpMethod;
	}

	public void setDoFilterOnHttpMethod(boolean doFilterOnHttpMethod) {
		this.doFilterOnHttpMethod = doFilterOnHttpMethod;
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

	public void setClickpathReportStepDurations(boolean clickpathReportStepDurations) {
		this.clickpathReportStepDurations = clickpathReportStepDurations;
	}

	public boolean isClickpathReportStepDurations() {
		return clickpathReportStepDurations;
	}

	public void setRemoveParametersFromUrl(boolean removeParametersFromUrl) {
		this.removeParametersFromUrl = removeParametersFromUrl;
	}

	public boolean isRemoveParametersFromUrl() {
		return removeParametersFromUrl;
	}

	public void setLogPattern(String logPattern) {
		this.logPattern = logPattern;
	}

	public String getLogPattern() {
		return logPattern;
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

    public CommandAccessLog.LogType getLogType() {
        return logType;
    }

    public void setLogType(CommandAccessLog.LogType logType) {
        this.logType = logType;
    }

    public List<String> getGroupByFields() {
        return groupByFields;
    }

    public void setGroupByFields(List<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    public void setClickpathEndOfSessionSnippet(String clickpathEndOfSessionSnippet) {
        this.clickpathEndOfSessionSnippet = clickpathEndOfSessionSnippet;
    }

    public String getClickpathEndOfSessionSnippet() {
        return clickpathEndOfSessionSnippet;
    }

    @Override
    public String toString() {
        return "AccessLogConfig{" +
                "showBasicUrls=" + showBasicUrls +
                ", showUserAgents=" + showUserAgents +
                ", showReferers=" + showReferers +
                ", ignoreMultiAndNoMatches=" + ignoreMultiAndNoMatches +
                ", doCountMultipleMapperHits=" + doCountMultipleMapperHits +
                ", excludeMappersInIisAndAccessLogs=" + excludeMappersInIisAndAccessLogs +
                ", doFilterOnHttpMethod=" + doFilterOnHttpMethod +
                ", doFilterOnHttpStatus=" + doFilterOnHttpStatus +
                ", mappers=" + mappers +
                ", countNoMappersAsOne=" + countNoMappersAsOne +
                ", clickpathReportStepDurations=" + clickpathReportStepDurations +
                ", clickpathEndOfSessionSnippet=" + clickpathEndOfSessionSnippet +
                ", removeParametersFromUrl=" + removeParametersFromUrl +
                ", logPattern='" + logPattern + '\'' +
                ", determineClickpaths=" + determineClickpaths +
                ", determineSessionDuration=" + determineSessionDuration +
                ", sessionField='" + sessionField + '\'' +
                ", sessionFieldRegexp='" + sessionFieldRegexp + '\'' +
                ", logType=" + logType +
                ", groupByFields='" + groupByFields + '\'' +
                '}';
    }
}
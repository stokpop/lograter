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

import nl.stokpop.lograter.command.CommandAccessLog;

import java.util.Collections;
import java.util.List;

public class AccessLogConfig extends MapperAndClickPathConfig {

	private boolean showBasicUrls = false;
	private boolean showUserAgents = false;
	private boolean showReferers = false;
	private boolean excludeMappersInIisAndAccessLogs = false;
	private boolean doFilterOnHttpMethod = false;
	private boolean doFilterOnHttpStatus = false;
	private boolean removeParametersFromUrl = false;
	private String logPattern = null;
	protected List<String> groupByFields = Collections.emptyList();
	private CommandAccessLog.LogType logType = CommandAccessLog.LogType.apache;

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
    public AccessLogConfig() {
        setFailureAwareAnalysis(true);
        setIncludeFailedHitsInAnalysis(true);
    }

	public List<String> getGroupByFields() {
		return groupByFields;
	}

	public void setGroupByFields(List<String> groupByFields) {
		this.groupByFields = groupByFields;
	}

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

	public CommandAccessLog.LogType getLogType() {
        return logType;
    }

    public void setLogType(CommandAccessLog.LogType logType) {
        this.logType = logType;
    }

	@Override
	public String toString() {
		return "AccessLogConfig{" +
			"showBasicUrls=" + showBasicUrls +
			", showUserAgents=" + showUserAgents +
			", showReferers=" + showReferers +
			", excludeMappersInIisAndAccessLogs=" + excludeMappersInIisAndAccessLogs +
			", doFilterOnHttpMethod=" + doFilterOnHttpMethod +
			", doFilterOnHttpStatus=" + doFilterOnHttpStatus +
			", removeParametersFromUrl=" + removeParametersFromUrl +
			", logPattern='" + logPattern + '\'' +
			", logType=" + logType +
			"} " + super.toString();
	}
}
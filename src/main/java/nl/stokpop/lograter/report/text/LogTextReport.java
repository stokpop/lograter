/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.report.text;

import nl.stokpop.lograter.analysis.ResponseTimeAnalyser;
import nl.stokpop.lograter.processor.BasicLogConfig;
import nl.stokpop.lograter.processor.BasicLogData;
import nl.stokpop.lograter.report.LogReport;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public abstract class LogTextReport implements LogReport {
	
	// Mind dor future refactorings: better use composite structure instead of inheritance structure.
	// Why extends is evil: http://www.javaworld.com/javaworld/jw-08-2003/jw-0801-toolbox.html?page=1
	
	private static final Locale DEFAULT_LOCALE = Locale.US;
	
	protected static final char SEP_CHAR = ',';
	protected static final Pattern SEP_CHAR_PATTERN = Pattern.compile(String.valueOf(SEP_CHAR));

	protected final DecimalFormat nfTwoDecimals;
	protected final DecimalFormat nfNoDecimals;
	protected final DecimalFormat nfDoNotShowDecimalSepAlways;
	
	public LogTextReport() {

		nfTwoDecimals = (DecimalFormat) DecimalFormat.getInstance(DEFAULT_LOCALE);
		nfTwoDecimals.applyPattern("#0.00");

		nfNoDecimals = (DecimalFormat) DecimalFormat.getInstance(DEFAULT_LOCALE);
		nfNoDecimals.applyPattern("0");

		nfDoNotShowDecimalSepAlways = (DecimalFormat) DecimalFormat.getInstance(DEFAULT_LOCALE);
		nfDoNotShowDecimalSepAlways.setDecimalSeparatorAlwaysShown(false);

	}

	public abstract void report(PrintWriter out, TimePeriod analysisPeriod);

	public String reportSummaryHeader(ResponseTimeAnalyser analyser, BasicLogConfig config) {
		TimePeriod filterPeriod = config.getFilterPeriod();
		TimePeriod analysisTimePeriod = analyser.getAnalysisTimePeriod();

		return reportSummaryHeader(filterPeriod, analysisTimePeriod, config.getRunId());
	}

	private String reportSummaryHeader(TimePeriod logFileParsePeriod, TimePeriod analysisPeriod, String runId) {
		StringBuilder str = new StringBuilder();

		if (logFileParsePeriod.isStartTimeSet() || logFileParsePeriod.isEndTimeSet()) {
			String startTimeString = logFileParsePeriod.isStartTimeSet() ? DateUtils.formatToStandardDateTimeString(logFileParsePeriod.getStartTime()) : "(not set)";
			String endTimeString = logFileParsePeriod.isEndTimeSet() ? DateUtils.formatToStandardDateTimeString(logFileParsePeriod.getEndTime()) : "(not set)";
			str.append("Log file parsed from ").append(startTimeString);
			str.append(" to ").append(endTimeString).append("\n");
			str.append("Duration ").append(logFileParsePeriod.getHumanReadableDuration()).append("\n");
		}

		str.append("Analysis period from ").append(DateUtils.formatToStandardDateTimeString(analysisPeriod.getStartTime()));
		str.append(" to ").append(DateUtils.formatToStandardDateTimeString(analysisPeriod.getEndTime())).append("\n");
		str.append("Duration ").append(analysisPeriod.getHumanReadableDuration()).append("\n");

		str.append("RunId: ").append(runId).append("\n");

		return str.toString();
	}

	public String reportSummaryHeader(BasicLogData data, BasicLogConfig config) {
		TimePeriod filterPeriod = config.getFilterPeriod();
		TimePeriod logPeriod = data.getLogTimePeriod();
		return reportSummaryHeader(filterPeriod, logPeriod, config.getRunId());
    }

}

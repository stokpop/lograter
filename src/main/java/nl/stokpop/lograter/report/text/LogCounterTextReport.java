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

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.analysis.*;
import nl.stokpop.lograter.command.BaseUnit;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.counter.RequestCounterPair;
import nl.stokpop.lograter.processor.BasicCounterLogConfig;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.store.TimeMeasurementStoreInMemory;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.linemapper.LineMap;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

import static nl.stokpop.lograter.store.RequestCounterStoreMaxCounters.OVERFLOW_COUNTER_NAME;

abstract class LogCounterTextReport extends LogTextReport {

	private static final Logger log = LoggerFactory.getLogger(LogCounterTextReport.class);

	private String histogramToString(HistogramData histogramData) {
		StringBuilder out = new StringBuilder();
		out.append("\n");
		double[] xValues = histogramData.getXvalues();
		double[] yValues = histogramData.getYvalues();
		for (int i = 0; i < xValues.length; i++) {
			out.append("period: ").append(xValues[i]).append(" number: ").append(yValues[i]).append("\n");
		}
		return out.toString();
	}

	public String reportCounters(String itemName, RequestCounterStorePair requestCounterStorePair, ResponseTimeAnalyser totalAnalyser, BasicCounterLogConfig config) {
		return reportCounters(itemName, requestCounterStorePair, totalAnalyser, config, Collections.emptyMap());
	}

	public String reportCounters(String itemName, RequestCounterStorePair counterStorePair, ResponseTimeAnalyser totalAnalyser, BasicCounterLogConfig config, Map<CounterKey, LineMap> keyToLineMap) {

		if (keyToLineMap == null) {
            throw new NullPointerException("CounterKeyToLineMapMap may not be null");
        }

        if ( counterStorePair.isEmpty()) {
            return String.format("No counters found to report in counter store pair [%s]%n", counterStorePair);
        }

        StringBuilder report = new StringBuilder(256);

        report.append(reportHeaderLine(itemName, config, false));

        TimePeriod analysisPeriod = totalAnalyser.getAnalysisTimePeriod();
        long maxTpmTimestamp = totalAnalyser.maxHitsPerMinute().getMaxHitsPerDurationTimestamp();
        long overallTotalHits = totalAnalyser.totalHits();

		for (RequestCounter successCounter : counterStorePair.getRequestCounterStoreSuccess()) {
			CounterKey counterKey = successCounter.getCounterKey();
			RequestCounter failureCounter = counterStorePair.getRequestCounterStoreFailure().get(counterKey);
            if (failureCounter == null) {
				// WORKAROUND: seems to happen now for complex OVERFLOW counters, e.g. with http status and method
				// these are not added to both failure and success counters during overflow: FIX!
				if (counterKey.getName().contains(OVERFLOW_COUNTER_NAME)) {
					failureCounter =  new RequestCounter(counterKey, new TimeMeasurementStoreInMemory());
				}
				else {
					throw new LogRaterException("No failure counter found for " + counterKey + " in " + counterStorePair);
				}
            }
			ResponseTimeAnalyser myAnalyser = ResponseTimeAnalyserFactory.createAnalyser(config, analysisPeriod, new RequestCounterPair(counterKey, successCounter, failureCounter));

			if (!myAnalyser.hasAnyHits()) {
				log.warn("Skipping line because there are no hits and failures at all for the counter in the analysis period [{}].", counterKey);
			}
			else {
				report.append(reportLine(myAnalyser, maxTpmTimestamp, overallTotalHits, config, keyToLineMap));
			}
		}
		return report.toString();
	}


    String reportCounter(String itemName, ResponseTimeAnalyser analyser, ResponseTimeAnalyser totalAnalyser, BasicCounterLogConfig config) {

		StringBuilder report = new StringBuilder();

		report.append(reportHeaderLine(itemName, config, false));

		int columns = StringUtils.countOccurrences(itemName, ',');
		long maxTpmTimestamp = totalAnalyser.maxHitsPerMinute().getMaxHitsPerDurationTimestamp();
		long overallTotalHits = totalAnalyser.totalHits();

		report.append(reportLine(analyser, maxTpmTimestamp, overallTotalHits, config, columns));

		return report.toString();
	}

	String reportCounter(String title, ResponseTimeAnalyser analyser, long maxTpmStartTimeStamp, long overallTotalHits, BasicCounterLogConfig config) {
        return reportHeaderLine(title, config, true) + reportLine(analyser, maxTpmStartTimeStamp, overallTotalHits, config);
	}

	private String reportHeaderLine(String name, BasicCounterLogConfig config, boolean overallHeader) {
		
		StringBuilder report = new StringBuilder(10 * 1024);
				
		report.append(name);

		if (!overallHeader) { reportHeaderCounterDetails(report); }

        if (config.isIncludeMapperRegexpColumn()) report.append(SEP_CHAR).append("regexp");

        boolean failureAwareAnalysis = config.isFailureAwareAnalysis();
        boolean includeFailedHitsInAnalysis = config.isIncludeFailedHitsInAnalysis();

        if (failureAwareAnalysis) {
            if (includeFailedHitsInAnalysis) {
                report.append(SEP_CHAR).append("hits incl failures");
            }
            else {
                report.append(SEP_CHAR).append("hits excl failures");
            }
            report.append(SEP_CHAR).append("failures");
            report.append(SEP_CHAR).append("failure%");
        }
        else {
            report.append(SEP_CHAR).append("hits");
        }
        
		BaseUnit baseUnit = config.getBaseUnit();

		report.append(SEP_CHAR).append("avg ").append(baseUnit.shortName()).append(SEP_CHAR);

		report.append("min").append(SEP_CHAR).append("max").append(SEP_CHAR);
		
		if (config.isCalculateStdDev()) report.append("std dev").append(SEP_CHAR);

		Double[] percentiles = config.getReportPercentiles();
		if (percentiles != null) {
		    for (Double percentile : percentiles) {
		        report.append(nfDoNotShowDecimalSepAlways.format(percentile)).append("% ").append(baseUnit.shortName()).append(SEP_CHAR);
            }
        }
		
		if (config.isCalculateHitsPerSecond()) report.append("max TPS").append(SEP_CHAR).append("max TPS ts").append(SEP_CHAR);

        report.append("avg TPS").append(SEP_CHAR);
		
		report.append("max TPM").append(SEP_CHAR);

		report.append("avg TPS max TPM").append(SEP_CHAR);

		report.append("max TPM ts").append(SEP_CHAR);

		report.append("TPM in overall max TPM").append(SEP_CHAR);

		report.append("TPS in overall max TPM").append(SEP_CHAR);

        report.append("overall%");

		if (config.isCalculateConcurrentCalls()) report.append(SEP_CHAR).append("max concur").append(SEP_CHAR).append("max concur ts");
		
        if (config.isCalculateStubDelays()) report.append(SEP_CHAR).append("stub-min").append(SEP_CHAR).append("stub-max").append(SEP_CHAR).append("stub-variance");

		report.append("\n");
		
		return report.toString();
	}

	abstract void reportHeaderCounterDetails(StringBuilder report);

    private String reportLine(ResponseTimeAnalyser analyser, long maxTpmStartTimeStamp, long totalHits, BasicCounterLogConfig config) {
        return reportLine(analyser, maxTpmStartTimeStamp, totalHits, config, 0, Collections.emptyMap());
    }

	private String reportLine(ResponseTimeAnalyser analyser, long maxTpmStartTimeStamp, long totalHits, BasicCounterLogConfig config, Map<CounterKey, LineMap> keyToLineMap) {
		return reportLine(analyser, maxTpmStartTimeStamp, totalHits, config, 0, keyToLineMap);
	}

    private String reportLine(ResponseTimeAnalyser analyser, long maxTpmStartTimeStamp, long totalHits, BasicCounterLogConfig config, int insertColumns) {
        return reportLine(analyser, maxTpmStartTimeStamp, totalHits, config, insertColumns, Collections.emptyMap());
    }

    private String reportLine(ResponseTimeAnalyser analyser, long maxTpmStartTimeStamp, long totalHits, BasicCounterLogConfig config, int insertColumns, Map<CounterKey, LineMap> counterKeyToLineMapMap) {
		StringBuilder report = new StringBuilder(256);

		CounterKey counterKey = analyser.getCounterKey();
		String counterKeyExcelProof = StringUtils.excelProofText(counterKey.getName());
		String nameWithColumnAlignment = RequestCounter.createCounterNameThatAlignsInTextReport(counterKeyExcelProof, insertColumns);
		report.append(nameWithColumnAlignment);

        if (config.isIncludeMapperRegexpColumn()) {
            LineMap lineMap = counterKeyToLineMapMap.get(counterKey);
            report.append(SEP_CHAR).append(lineMap == null ? "" : replaceSepChars(lineMap.getRegExpPattern()));
        }

		long hitsCount = analyser.totalHits();

        // hits
		report.append(SEP_CHAR).append(nfNoDecimals.format(hitsCount));

	    if (analyser instanceof FailureAware) {
		    long hitsCountFailure = ((FailureAware) analyser).failedHits();
			// failures
		    report.append(SEP_CHAR).append(nfNoDecimals.format(hitsCountFailure));
			double failurePercentage =  ((FailureAware) analyser).failurePercentage();
		    // failure%
			report.append(SEP_CHAR).append(nfTwoDecimals.format(failurePercentage));
	    }

        final double avgHitDuration = analyser.avgHitDuration();
	    // avg
        report.append(SEP_CHAR).append(nfNoDecimals.format(avgHitDuration));
        // min
		report.append(SEP_CHAR).append(nfNoDecimals.format(analyser.min()));
		// max
		report.append(SEP_CHAR).append(nfNoDecimals.format(analyser.max()));
        final double stdDevHitDuration = analyser.stdDevHitDuration();
        // std dev
        if (config.isCalculateStdDev()) report.append(SEP_CHAR).append(nfNoDecimals.format(stdDevHitDuration));
        //percentiles
        Double[] percentiles = config.getReportPercentiles();
        if (percentiles != null) {
            for (Double percentile : percentiles) {
                report.append(SEP_CHAR).append(nfNoDecimals.format(analyser.percentileHitDuration(percentile)));
            }
        }

		if (config.isCalculateHitsPerSecond()) {
			TransactionCounterResult tps = analyser.maxHitsPerSecond();
			// max TPS
			report.append(SEP_CHAR).append(nfNoDecimals.format(tps.getMaxHitsPerDuration()));
			// max TPS ts
			report.append(SEP_CHAR).append(tps.getMaxHitsPerDuration() > 1 ? DateUtils.formatToStandardDateTimeString(tps.getMaxHitsPerDurationTimestamp()) : "");
		}
        // avg TPS
		report.append(SEP_CHAR).append(nfTwoDecimals.format(analyser.avgTps()));

		TransactionCounterResult tcr = analyser.maxHitsPerMinute();
	    long maxHitsPerMinute = tcr.getMaxHitsPerDuration();
	    // max TPM
	    report.append(SEP_CHAR).append(nfNoDecimals.format(maxHitsPerMinute));
		// avg TPS max TPM
		report.append(SEP_CHAR).append(nfTwoDecimals.format(maxHitsPerMinute / 60.0d));
		// max TPM ts
		report.append(SEP_CHAR).append(maxHitsPerMinute > 1 ? DateUtils.formatToStandardDateTimeString(tcr.getMaxHitsPerDurationTimestamp()) : "");
	    final long hitsInMinuteOverallMaxTPM = analyser.hitsInMinuteWithStartTime(maxTpmStartTimeStamp);
	    // TPM in overall max TPM
	    report.append(SEP_CHAR).append(nfNoDecimals.format(hitsInMinuteOverallMaxTPM));
	    // avg TPS in overall max TPM
	    report.append(SEP_CHAR).append(nfTwoDecimals.format(hitsInMinuteOverallMaxTPM / 60.0d));
        // % overall
        report.append(SEP_CHAR).append(nfTwoDecimals.format(analyser.percentage(totalHits)));

		if (config.isCalculateConcurrentCalls()) {
			ConcurrentCounterResult ccr = analyser.maxConcurrentRequests();
			// max concur
			report.append(SEP_CHAR).append(nfNoDecimals.format(ccr.maxConcurrentRequests));
			// max concur ts
			report.append(SEP_CHAR).append(ccr.maxConcurrentRequests > 1 ? DateUtils.formatToStandardDateTimeString(ccr.maxConcurrentRequestsTimestamp) : "");
		}

        if (config.isCalculateStubDelays()) {
            double stubMin = avgHitDuration - stdDevHitDuration;
            double stubMax = avgHitDuration + stdDevHitDuration;
            int variance = 1;

            if (stubMin < 0.0d) {
                stubMax = stubMax - stubMin;
                stubMin = 0.0d;
            }

            report.append(SEP_CHAR).append(nfNoDecimals.format(stubMin));
            report.append(SEP_CHAR).append(nfNoDecimals.format(stubMax));
            report.append(SEP_CHAR).append(variance);

        }

        report.append("\n");
		
		if (log.isTraceEnabled()) {
			log.trace("histogram for {}: {}", counterKey, histogramToString(analyser.histogramForRelevantValues(ResponseTimeAnalyserFailureUnaware.GRAPH_HISTO_NUMBER_OF_RANGES)));
		}
		
		return report.toString();
		
	}

	private String replaceSepChars(String textWithPossibleSepChars) {
		return SEP_CHAR_PATTERN.matcher(textWithPossibleSepChars).replaceAll("§");
	}

}

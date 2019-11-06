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
package nl.stokpop.lograter.report.json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.stokpop.lograter.analysis.FailureAware;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyser;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserFailureUnaware;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserFailureUnaware.ConcurrentCounterResult;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserFailureUnaware.TransactionCounterResult;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserWithFailedHits;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserWithoutFailedHits;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.counter.RequestCounterPair;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

class LogCounterJsonReport {

    private static final Locale DEFAULT_LOCALE = Locale.US;

	private static final Logger log = LoggerFactory.getLogger(LogCounterJsonReport.class);

    private final DecimalFormat nfTwoDecimals;
    private final DecimalFormat nfNoDecimals;

    private final boolean addStubDelays;

    LogCounterJsonReport() {
    	this(false);
    }

    private LogCounterJsonReport(boolean addStubDelays) {
        nfTwoDecimals = (DecimalFormat) NumberFormat.getInstance(DEFAULT_LOCALE);
        nfTwoDecimals.applyPattern("#0.00");

        nfNoDecimals = (DecimalFormat) NumberFormat.getInstance(DEFAULT_LOCALE);
        nfNoDecimals.applyPattern("0");

        this.addStubDelays = addStubDelays;
	}

    void addParseAndAnalysisPeriods(ObjectNode node, TimePeriod totalPeriod, TimePeriod analysisPeriod) {

        if (totalPeriod.isStartTimeSet() || totalPeriod.isEndTimeSet()) {
            String startTimeString = totalPeriod.isStartTimeSet() ? DateUtils.formatToStandardDateTimeString(totalPeriod.getStartTime()) : "(not set)";
            String endTimeString = totalPeriod.isEndTimeSet() ? DateUtils.formatToStandardDateTimeString(totalPeriod.getEndTime()) : "(not set)";
            node.put("parsePeriodStartTime", startTimeString);
            node.put("parsePeriodEndTime", endTimeString);
            node.put("parsePeriodDuration", totalPeriod.getHumanReadableDuration());
        }

        node.put("analysisPeriodStartTime", DateUtils.formatToStandardDateTimeString(analysisPeriod.getStartTime()));
        node.put("analysisPeriodEndTime", DateUtils.formatToStandardDateTimeString(analysisPeriod.getEndTime()));
        node.put("analysisPeriodDuration", analysisPeriod.getHumanReadableDuration());

    }

	void reportCounters(ObjectNode node, RequestCounterStorePair pair, ResponseTimeAnalyser analyser) {

        final RequestCounterStore storeSuccess = pair.getRequestCounterStoreSuccess();
        final RequestCounterStore storeFailure = pair.getStoreFailure();

        ArrayNode arrayNode = node.putArray("counters");

        TimePeriod analysisPeriod = analyser.getAnalysisTimePeriod();
        long maxTpmTimestamp = analyser.maxHitsPerMinute().getMaxHitsPerDurationTimestamp();
        long overallTotalHits = analyser.totalHits();

        for (RequestCounter successCounter : storeSuccess) {
			RequestCounter analysisPeriodSuccessCounter = successCounter.getTimeSlicedCounter(analysisPeriod);
			boolean includeFailuresInAnalysis = analyser instanceof ResponseTimeAnalyserWithFailedHits;
			ResponseTimeAnalyser myAnalyser = findMatchingFailureAnalyserForSuccessCounter(storeFailure, analysisPeriod, analysisPeriodSuccessCounter, includeFailuresInAnalysis);
            if (myAnalyser.hasAnyHits()) {
                ObjectNode counterNode = arrayNode.addObject();
                createCounterNode(counterNode, myAnalyser, maxTpmTimestamp, overallTotalHits);
            } else {
                log.warn("Skipping line because there are no hits at all " +
                        "for the counter in the analysis period [{}].", successCounter.getCounterKey());
            }
		}
	}

    private ResponseTimeAnalyser findMatchingFailureAnalyserForSuccessCounter(RequestCounterStore storeFailure,
                                                                              TimePeriod analysisPeriod,
                                                                              RequestCounter successCounter,
                                                                              boolean includeFailuresInAnalysis) {
        ResponseTimeAnalyser myAnalyser;
        if (storeFailure == null || storeFailure.get(successCounter.getCounterKey()) == null) {
            myAnalyser = new ResponseTimeAnalyserFailureUnaware(successCounter, analysisPeriod);
        }
        else {
            RequestCounter failureCounter = storeFailure.get(successCounter.getCounterKey());
            RequestCounterPair pair = new RequestCounterPair(successCounter, failureCounter);
            if (includeFailuresInAnalysis) {
                myAnalyser = new ResponseTimeAnalyserWithFailedHits(pair, analysisPeriod);
            }
            else {
                myAnalyser = new ResponseTimeAnalyserWithoutFailedHits(pair, analysisPeriod);
            }
        }
        return myAnalyser;
    }

	void reportCounters(ObjectNode node, RequestCounterStore store, ResponseTimeAnalyser totalAnalyser) {
	 	reportCounters(node, new RequestCounterStorePair(store, null), totalAnalyser);
	}

	void reportOverallCounter(ObjectNode node, ResponseTimeAnalyser analyser, long maxTpmStartTimeStamp, long overallTotalHits) {
        ObjectNode counterNode = node.putObject("overall-counter");
        createCounterNode(counterNode, analyser, maxTpmStartTimeStamp, overallTotalHits);
	}

    private void createCounterNode(ObjectNode node, ResponseTimeAnalyser analyser, long maxTpmStartTimeStamp, long totalHits) {
		node.put("name", analyser.getCounterKey());
        long hits = analyser.totalHits();
        node.put("hits", nfNoDecimals.format(hits));
        if (analyser instanceof FailureAware) {
	        FailureAware analyserWithFailures = (FailureAware) analyser;
	        node.put("failures", nfNoDecimals.format(analyserWithFailures.failedHits()));
        	node.put("failurePercentage", nfTwoDecimals.format(analyserWithFailures.failurePercentage()));
        }
        final double avgHitDuration = analyser.avgHitDuration();
        node.put("avgHitDurationMillis", nfNoDecimals.format(avgHitDuration));
		node.put("minHitDurationMillis", nfNoDecimals.format(analyser.min()));
		node.put("maxHitDurationMillis", nfNoDecimals.format(analyser.max()));
        final double stdDevHitDuration = analyser.stdDevHitDuration();
        node.put("stdDevHitDurationMillis", nfNoDecimals.format(stdDevHitDuration));
		node.put("percentile95HitDurationMillis", nfNoDecimals.format(analyser.percentileHitDuration(95)));
		node.put("percentile99HitDurationMillis", nfNoDecimals.format(analyser.percentileHitDuration(99)));
        final TransactionCounterResult tps = analyser.maxHitsPerSecond();
		node.put("maxHitsPerSecond", nfNoDecimals.format(tps.getMaxHitsPerDuration()));
		node.put("maxHitsPerSecondTimestamp", tps.getMaxHitsPerDuration() > 1 ? DateUtils.formatToStandardDateTimeString(tps.getMaxHitsPerDurationTimestamp()) : "");
		node.put("avgHitsPerSecondWholePeriod", nfTwoDecimals.format(analyser.avgTps()));
		TransactionCounterResult tpm = analyser.maxHitsPerMinute();
		node.put("maxHitsPerMinute", nfNoDecimals.format(tpm.getMaxHitsPerDuration()));
		node.put("maxHitsPerMinuteHitsPerSecond", nfTwoDecimals.format((double) tpm.getMaxHitsPerDuration() / 60.0d));
		node.put("maxHitsPerMinuteTimestamp", tpm.getMaxHitsPerDuration() > 1 ? DateUtils.formatToStandardDateTimeString(tpm.getMaxHitsPerDurationTimestamp()) : "");

	    long hitsInOverallMaxMinute = analyser.hitsInMinuteWithStartTime(maxTpmStartTimeStamp);
	    node.put("hitsInOverallMaxHitsPerMinute", nfTwoDecimals.format(hitsInOverallMaxMinute));
		node.put("hitsPerSecondInOverallMaxHitsPerMinute", nfTwoDecimals.format((double) hitsInOverallMaxMinute / 60.0d));
        node.put("percentageInOverallMaxHitsPerMinute", nfTwoDecimals.format(analyser.percentage(totalHits)));

        ConcurrentCounterResult ccr = analyser.maxConcurrentRequests();
		node.put("maxConcurrentRequests", nfNoDecimals.format(ccr.maxConcurrentRequests));
		node.put("maxConcurrentRequestsTimestamp", ccr.maxConcurrentRequests > 1 ? DateUtils.formatToStandardDateTimeString(ccr.maxConcurrentRequestsTimestamp) : "");

        if (addStubDelays) {
            double stubMin = avgHitDuration - stdDevHitDuration;
            double stubMax = avgHitDuration + stdDevHitDuration;
            int variance = 1;

            if (stubMin < 0.0d) {
                stubMax = stubMax - stubMin;
                stubMin = 0.0d;
            }

            node.put("stubDelayMinMillis", nfNoDecimals.format(stubMin));
            node.put("stubDelayMaxMillis", nfNoDecimals.format(stubMax));
            node.put("stubVariance", variance);
        }
	}

}

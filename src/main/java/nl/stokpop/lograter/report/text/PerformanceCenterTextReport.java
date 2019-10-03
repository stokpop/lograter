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
package nl.stokpop.lograter.report.text;

import nl.stokpop.lograter.analysis.ResponseTimeAnalyser;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserWithFailuresExcludedInMetrics;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterAggregationGranularity;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterConfig;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterDataBundle;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.io.PrintStream;
import java.util.Locale;

import static nl.stokpop.lograter.processor.performancecenter.PerformanceCenterAggregationGranularity.AggregationGranularityType.LRA_FILE_EXACT;

/**
 * Because of the aggregation granularity of the database values not all response times are present.
 * The percentiles, concurrent calls and other calculated values are to be considered rough estimates
 * in the generated PerformanceCenter reports.
 * Try to use the smallest granularity as possible. The smallest granularity seems to be 1 second.
 */
public class PerformanceCenterTextReport extends LogCounterTextReport {

	private final PerformanceCenterDataBundle data;

	public PerformanceCenterTextReport(PerformanceCenterDataBundle data) {
		this.data = data;
	}

	@Override
	public void report(PrintStream out, TimePeriod analysisPeriod) {
		RequestCounter totalRequestCounterFailure = data.getTotalRequestCounterStorePair().getRequestCounterStoreFailure().getTotalRequestCounter();
		RequestCounter requestCounterFailureTotal = totalRequestCounterFailure.getTimeSlicedCounter(analysisPeriod);

		PerformanceCenterConfig config = data.getConfig();
		ResponseTimeAnalyser analyserTotal = new ResponseTimeAnalyserWithFailuresExcludedInMetrics(data.getTotalRequestCounterStorePair().getTotalRequestCounterPair(), analysisPeriod);

		out.println(reportSummaryHeader(analyserTotal, config));
		out.println(reportAggregationDetails(data.getAggregationGranularity()));
		out.println();
		out.println(reportCounter(config.getCounterFields(), analyserTotal, analyserTotal, config));

		// one store expected for performance center data
		for (RequestCounterStorePair requestCounterStore : data.getRequestCounterStorePairs()) {
		    out.println(reportCounters(config.getCounterFields(), requestCounterStore, analyserTotal, config));
		}

        ResponseTimeAnalyser analyserFailuresTotal = new ResponseTimeAnalyser(requestCounterFailureTotal, analysisPeriod);
        PerformanceCenterConfig failureConfig = new PerformanceCenterConfig();
        failureConfig.setFailureAwareAnalysis(false);
        failureConfig.setFailureAwareAnalysisIncludeFailuresInMetrics(false);

        out.println("FAILURES");
        out.println(reportCounter(config.getCounterFields(), analyserFailuresTotal, analyserFailuresTotal, failureConfig));

        // one store expected for performance center data
        for (RequestCounterStorePair requestCounterStore : data.getRequestCounterStorePairs()) {
            out.println(reportFailureCounters(config.getCounterFields(), requestCounterStore.getRequestCounterStoreFailure(), analyserFailuresTotal, failureConfig));
        }
		
	}

    private String reportAggregationDetails(PerformanceCenterAggregationGranularity granularity) {
		String warning = granularity.getGranularitySeconds() > 1.0 ?
				"%nNote: shorter aggregation granularity gives more precise percentile calculations." +
				"%nThe aggregation granularity can be changed in the performance center analysis template for your runs." : "";
		String granularityType = granularity.getType() == LRA_FILE_EXACT ? "Results.lra file (exact)" : "Results.[m]db (estimate)";
		return String.format(Locale.US, "Performance center analysis database granularity is %.2f seconds, " +
				"determined from %s." + warning, granularity.getGranularitySeconds(), granularityType);
    }

    @Override
	void reportHeaderCounterDetails(StringBuilder report) {
		// no details for the header in this report
	}

}

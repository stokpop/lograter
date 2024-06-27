/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserFactory;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterAggregationGranularity;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterConfig;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterDataBundle;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.io.PrintWriter;
import java.util.Locale;

import static nl.stokpop.lograter.processor.performancecenter.PerformanceCenterAggregationGranularity.AggregationGranularityType.LRA_FILE_EXACT;

/**
 * Because of the aggregation granularity of the database values, not all single response times are present.
 *
 * The percentiles, concurrent calls and other calculated values are to be considered rough estimates
 * in the generated PerformanceCenter reports.
 *
 * Try to use the smallest granularity as possible.
 * The smallest granularity seems to be 1 second.
 */
public class PerformanceCenterTextReport extends LogCounterTextReport {

	private final PerformanceCenterDataBundle data;

	public PerformanceCenterTextReport(PerformanceCenterDataBundle data) {
		this.data = data;
	}

	@Override
	public void report(PrintWriter out, TimePeriod analysisPeriod) {
        RequestCounterStorePair pair = data.getTotalRequestCounterStorePair();

		PerformanceCenterConfig config = data.getConfig();
		ResponseTimeAnalyser analyser = ResponseTimeAnalyserFactory.createAnalyser(config, analysisPeriod, pair.getTotalRequestCounterPair());

		out.println(reportSummaryHeader(analyser, config));
		out.println(reportAggregationDetails(data.getAggregationGranularity()));
		out.println();
		out.println(reportCounter(config.getCounterFields(), analyser, analyser, config));

		// one store pair expected for performance center data
		for (RequestCounterStorePair requestCounterStore : data.getRequestCounterStorePairs()) {
		    out.println(reportCounters(config.getCounterFields(), requestCounterStore, analyser, config));
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

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
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserFactory;
import nl.stokpop.lograter.processor.accesslog.AccessLogConfig;
import nl.stokpop.lograter.processor.accesslog.AccessLogDataBundle;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.io.PrintStream;

public class AccessLogTextReport extends LogCounterTextReport {

	final private AccessLogConfig config;
    final private AccessLogDataBundle dataBundle;

    public

    AccessLogTextReport(AccessLogDataBundle accessLogDataBundle) {
        this.dataBundle = accessLogDataBundle;
        this.config = accessLogDataBundle.getConfig();
    }

    @Override
    protected void reportHeaderCounterDetails(StringBuilder report) {
		if (config.groupByHttpMethod()) report.append(SEP_CHAR).append("http-method");
		if (config.groupByHttpStatus()) report.append(SEP_CHAR).append("http-status");
		for (String field : config.getGroupByFields()) {
            report.append(SEP_CHAR).append(field);
        }
	}

    @Override
    public void report(PrintStream out, TimePeriod analysisPeriod) {
        RequestCounterStorePair pair = dataBundle.getTotalRequestCounterStorePair();

        ResponseTimeAnalyser analyser = ResponseTimeAnalyserFactory.createAnalyser(config, analysisPeriod, pair.getTotalRequestCounterPair());

        out.println(reportSummaryHeader(analyser, config));

        long maxTpmStartTimeStamp = analyser.maxHitsPerMinute().getMaxHitsPerDurationTimestamp();
        long overallTotalHits = analyser.totalHits();

        out.println(reportCounter("counter", analyser, maxTpmStartTimeStamp, overallTotalHits, config));

        for (RequestCounterStorePair storePair : dataBundle.getRequestCounterStorePairs()) {
	        String storeSuccessName = storePair.getRequestCounterStoreSuccess().getName();
	        out.println(reportCounters(storeSuccessName, storePair, analyser, config, dataBundle.getCounterKeyToLineMapMap()));
        }

    }

}

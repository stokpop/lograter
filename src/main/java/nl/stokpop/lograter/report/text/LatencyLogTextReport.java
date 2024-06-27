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
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserFailureUnaware;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserWithFailedHits;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.processor.latency.LatencyLogConfig;
import nl.stokpop.lograter.processor.latency.LatencyLogDataBundle;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.io.PrintWriter;

public class LatencyLogTextReport extends LogCounterTextReport {

    private final LatencyLogDataBundle data;

    public LatencyLogTextReport(LatencyLogDataBundle data) {
        this.data = data;
    }

    @Override
    public void report(PrintWriter out, TimePeriod analysisPeriod) {

        RequestCounter totalRequestCounter = data.getTotalRequestCounterStorePair().getRequestCounterStoreSuccess().getTotalRequestCounter();
        RequestCounter analysisTotalRequestCounter = totalRequestCounter.getTimeSlicedCounter(analysisPeriod);

        LatencyLogConfig config = data.getConfig();
        ResponseTimeAnalyser analyserTotal = config.isFailureAwareAnalysis()
            ? new ResponseTimeAnalyserWithFailedHits(data.getTotalRequestCounterStorePair().getTotalRequestCounterPair(), analysisPeriod)
            : new ResponseTimeAnalyserFailureUnaware(analysisTotalRequestCounter, analysisPeriod);

        out.println(reportSummaryHeader(analyserTotal, config));
        String commaSeparatedCounterFields = String.join(",", config.getCounterFields());
        out.println(reportCounter(commaSeparatedCounterFields, analyserTotal, analyserTotal, config));
        for (RequestCounterStorePair requestCounterStorePair : data.getRequestCounterStorePairs()) {
            out.println(reportCounters(commaSeparatedCounterFields, requestCounterStorePair, analyserTotal, config, data.getKeyToLineMap()));
        }
    }

    @Override
    void reportHeaderCounterDetails(StringBuilder report) {
        // no header counter details for this report
    }

}

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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyser;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserFailureUnaware;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserWithFailedHits;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserWithoutFailedHits;
import nl.stokpop.lograter.counter.RequestCounterDataBundle;
import nl.stokpop.lograter.counter.RequestCounterPair;
import nl.stokpop.lograter.processor.BasicLogConfig;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterDataBundle;
import nl.stokpop.lograter.report.LogReport;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;

public class RequestCounterJsonReport implements LogReport {

	private static final Logger log = LoggerFactory.getLogger(RequestCounterJsonReport.class);

	final private BasicLogConfig config;

    final private RequestCounterDataBundle dataBundle;

    final private LogCounterJsonReport jsonReport;

	final static private ObjectMapper mapper = new ObjectMapper();

    public RequestCounterJsonReport(RequestCounterDataBundle requestCounterDataBundle) {
        this.dataBundle = requestCounterDataBundle;
        this.config = requestCounterDataBundle.getConfig();
        this.jsonReport = new LogCounterJsonReport();
    }

    public void report(PrintStream out, TimePeriod analysisPeriod) throws IOException {

        JsonNodeFactory factory = new JsonNodeFactory(false);

        JsonFactory jsonFactory = new JsonFactory();
        JsonGenerator generator = jsonFactory.createGenerator(out);

        ObjectNode rootNode = factory.objectNode();
        rootNode.put("externalRunId", config.getRunId());

	    RequestCounterPair totalRequestCounterPair = dataBundle.getTotalRequestCounterStorePair().getTotalRequestCounterPair();

	    if (totalRequestCounterPair.isEmpty()) {
            log.warn("No hits available to generate report for [{}].", config.getRunId());
        }
        else {
            ResponseTimeAnalyser responseTimeAnalyser;

            if (dataBundle.doesSupportFailureRequestCounters()) {
                if (dataBundle instanceof PerformanceCenterDataBundle) {
                    responseTimeAnalyser = new ResponseTimeAnalyserWithoutFailedHits(totalRequestCounterPair, analysisPeriod);
                }
                else {
                    responseTimeAnalyser = new ResponseTimeAnalyserWithFailedHits(totalRequestCounterPair, analysisPeriod);
                }
            }
            else {
                responseTimeAnalyser = new ResponseTimeAnalyserFailureUnaware(totalRequestCounterPair.getCounterSuccess(), analysisPeriod);
            }

            if (responseTimeAnalyser.totalHits() == 0) {
                log.warn("No hits available in the analysis period [{}] to generate report for [{}].", analysisPeriod, config.getRunId());
            }
            else {
                jsonReport.addParseAndAnalysisPeriods(rootNode, config.getFilterPeriod(), responseTimeAnalyser.getAnalysisTimePeriod());

                long maxTpmStartTimeStamp = responseTimeAnalyser.maxHitsPerMinute().getMaxHitsPerDurationTimestamp();
                long overallTotalHits = responseTimeAnalyser.totalHits();

                jsonReport.reportOverallCounter(rootNode, responseTimeAnalyser, maxTpmStartTimeStamp, overallTotalHits);

                ArrayNode counterStoresNodes = rootNode.putArray("counterStores");
                for (RequestCounterStorePair storePair : dataBundle.getRequestCounterStorePairs()) {
                    ObjectNode counterStoreNode = counterStoresNodes.addObject();
                    RequestCounterStore successStore = storePair.getRequestCounterStoreSuccess();
                    counterStoreNode.put("name", successStore.getName());
                    if (dataBundle.doesSupportFailureRequestCounters()) {
                        jsonReport.reportCounters(counterStoreNode, storePair, responseTimeAnalyser);
                    } else {
                        jsonReport.reportCounters(counterStoreNode, successStore, responseTimeAnalyser);
                    }
                }
            }
	    }
	    mapper.writeTree(generator, rootNode);
    }

	public void report(PrintStream out) throws IOException {

        final TimePeriod totalTimePeriod = dataBundle.getTotalRequestCounterStorePair().totalTimePeriod();
        final TimePeriod filterPeriod = config.getFilterPeriod();
        final TimePeriod analysisPeriod = totalTimePeriod.createFilterTimePeriodIfFilterIsSet(filterPeriod);

        report(out, analysisPeriod);
    }

}

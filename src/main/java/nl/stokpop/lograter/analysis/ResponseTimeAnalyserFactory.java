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
package nl.stokpop.lograter.analysis;

import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.counter.RequestCounterPair;
import nl.stokpop.lograter.processor.BasicCounterLogConfig;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.util.time.TimePeriod;

public class ResponseTimeAnalyserFactory {
    /**
     * @return a response time analyser based on the config
     */
	public static ResponseTimeAnalyser createAnalyser(BasicCounterLogConfig config, TimePeriod analysisPeriod, RequestCounterPair counterPair) {
        boolean failureAwareAnalysis = config.isFailureAwareAnalysis();
        boolean includeFailedHitsInAnalysis = config.isIncludeFailedHitsInAnalysis();

        if (failureAwareAnalysis) {
		    if (includeFailedHitsInAnalysis) {
		        return new ResponseTimeAnalyserWithFailedHits(counterPair, analysisPeriod);
            }
            else {
                return new ResponseTimeAnalyserWithoutFailedHits(counterPair, analysisPeriod);
            }
        }
        else {
		    return new ResponseTimeAnalyserFailureUnaware(counterPair.getCombinedRequestCounter(), analysisPeriod);
        }
	}

	// TODO can we do this based on the config settings?
    public static ResponseTimeAnalyser findMatchingFailureAnalyserForSuccessCounter(RequestCounterStore storeFailure,
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

    public static ResponseTimeAnalyser createSimpleFailureUnaware(RequestCounter counter, TimePeriod period) {
        return new ResponseTimeAnalyserFailureUnaware(counter, period);
    }

    public static ResponseTimeAnalyser createSimpleFailureUnaware(RequestCounter counter) {
        return new ResponseTimeAnalyserFailureUnaware(counter);
    }
}

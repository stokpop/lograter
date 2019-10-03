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
package nl.stokpop.lograter.counter;

import nl.stokpop.lograter.util.time.TimePeriod;

public class RequestCounterPair {

    private final String name;
    private final RequestCounter counterSuccess;
    private final RequestCounter counterFailure;
    private final TimePeriod combinedTimePeriod;
    private final RequestCounter combinedRequestCounter;

    public RequestCounterPair(String name, RequestCounter counterSuccess, RequestCounter counterFailure) {
        this.name = name;
        this.counterSuccess = counterSuccess;
        this.counterFailure = counterFailure;
        this.combinedTimePeriod = TimePeriod.createMaxTimePeriod(counterSuccess.getTimePeriod(), counterFailure.getTimePeriod());
        this.combinedRequestCounter = new ReadOnlyRequestCounter(counterSuccess.getUniqueCounterKey(), counterSuccess, counterFailure, combinedTimePeriod);
    }

    public RequestCounterPair(RequestCounter counterSuccess, RequestCounter counterFailure) {
        this(counterSuccess.getUniqueCounterKey(), counterSuccess, counterFailure);
    }

    public String getName() {
        return name;
    }

    public RequestCounter getCounterSuccess() {
        return counterSuccess;
    }

    public RequestCounter getCounterFailure() {
        return counterFailure;
    }

    public RequestCounter getCombinedRequestCounter() {
        return combinedRequestCounter;
    }

    public TimePeriod getCombinedTimePeriod() {
        return combinedTimePeriod;
    }

    public long getCombinedHits() {
        return combinedRequestCounter.getHits();
    }

    public boolean isEmpty() {
        return counterFailure.getHits() == 0 && counterSuccess.getHits() == 0;
    }

}

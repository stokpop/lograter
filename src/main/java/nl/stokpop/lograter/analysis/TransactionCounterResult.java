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
package nl.stokpop.lograter.analysis;

import nl.stokpop.lograter.util.time.TPSMeasurement;

import java.util.Collections;
import java.util.List;

public class TransactionCounterResult {
    private final List<TPSMeasurement> tpsPerTimestamp;
    private final long maxHitsPerDuration;
    private final long durationInMillis;
    private final long maxHitsPerDurationTimestamp;

    TransactionCounterResult(List<TPSMeasurement> tpsPerTimestamp, long maxHitsPerDuration, long durationInMillis, long maxHitsPerDurationTimestamp) {
        this.tpsPerTimestamp = tpsPerTimestamp == null ? Collections.emptyList() : tpsPerTimestamp;
        this.maxHitsPerDuration = maxHitsPerDuration;
        this.durationInMillis = durationInMillis;
        this.maxHitsPerDurationTimestamp = maxHitsPerDurationTimestamp;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder( 128);
        sb.append("TransactionCounterResult{tpsPerTimestamp.size=").append(tpsPerTimestamp.size());
        sb.append(", maxHitsPerDuration=").append(maxHitsPerDuration);
        sb.append(", durationInMillis=").append(durationInMillis);
        sb.append(", maxHitsPerDurationTimestamp=").append(maxHitsPerDurationTimestamp);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

    public List<TPSMeasurement> getTpsPerTimestamp() {
        return tpsPerTimestamp;
    }

    public long getMaxHitsPerDuration() {
        return maxHitsPerDuration;
    }

    public long getDurationInMillis() {
        return durationInMillis;
    }

    public long getMaxHitsPerDurationTimestamp() {
        return maxHitsPerDurationTimestamp;
    }
}

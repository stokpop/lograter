/*
 * Copyright (C) 2020 Peter Paul Bakker, Stokpop Software Solutions
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
package nl.stokpop.lograter.processor.performancecenter;

public class PerformanceCenterAggregationGranularity {
    public enum AggregationGranularityType {
        DATABASE_GUESS, LRA_FILE_EXACT
    }

    private final long granularityMillis;
    private final AggregationGranularityType type;

    public PerformanceCenterAggregationGranularity(long granularityMillis, AggregationGranularityType type) {
        this.granularityMillis = granularityMillis;
        this.type = type;
    }

    public long getGranularityMillis() {
        return granularityMillis;
    }

    public double getGranularitySeconds() {
        return granularityMillis == 0 ? 0 : granularityMillis / 1000.0;
    }

    public AggregationGranularityType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PerformanceCenterAggregationGranularity{" +
                "granularityMillis=" + granularityMillis +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }
}

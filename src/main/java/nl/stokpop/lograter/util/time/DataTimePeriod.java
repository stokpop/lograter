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
package nl.stokpop.lograter.util.time;

import net.jcip.annotations.NotThreadSafe;

/**
 * Keeps track of the earliest and latest timestamps of a data set.
 * Note this class is not thread safe because it has changing state.
 */
@NotThreadSafe
public class DataTimePeriod {
    private long dataStartTime = 0L;
    private long dataEndTime = 0L;

    public DataTimePeriod() {
    }

    public TimePeriod getLogTimePeriod() {
        return TimePeriod.createIncludingEndTime(dataStartTime, dataEndTime);
    }

    public void updateDataTime(long timestamp) {
        updateLogStartTime(timestamp);
        updateLogEndTime(timestamp);
    }

    private void updateLogStartTime(long startTime) {
        if (this.dataStartTime == 0L) {
            this.dataStartTime = startTime;
        }
        if (this.dataStartTime > startTime) {
            this.dataStartTime = startTime;
        }
    }

    private void updateLogEndTime(long endTime) {
        if (this.dataEndTime < endTime) {
            this.dataEndTime = endTime;
        }
    }

    @Override
    public String toString() {
        return "DataTimePeriod{" +
                    TimePeriod.createIncludingEndTime(dataStartTime, dataEndTime).toString() +
                '}';
    }
}
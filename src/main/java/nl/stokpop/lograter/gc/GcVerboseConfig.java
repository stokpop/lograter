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
package nl.stokpop.lograter.gc;


import nl.stokpop.lograter.processor.BasicLogConfig;

public class GcVerboseConfig extends BasicLogConfig {
    private long analysisStartTime = 0L;
    private long analysisEndTime = Long.MAX_VALUE;
    private long memoryFitStartTime = 0L;
    private long memoryFitEndTime = Long.MAX_VALUE;

    public void setAnalysisStartTime(long analysisStartTime) {
        this.analysisStartTime = analysisStartTime;
    }

    public long getAnalysisStartTime() {
        return this.analysisStartTime;
    }

    public void setAnalysisEndTime(long analysisEndTime) {
        this.analysisEndTime = analysisEndTime;
    }

    public long getAnalysisEndTime() {
        return this.analysisEndTime;
    }

    public boolean isAnalysisTimePeriodSet() {
        return analysisStartTime != 0L && analysisEndTime != Long.MAX_VALUE;
    }

    public boolean isMemoryFitPeriodSet() {
        return memoryFitStartTime != 0L && memoryFitEndTime != Long.MAX_VALUE;
    }

    public long getMemoryFitStartTime() {
        return memoryFitStartTime;
    }

    public void setMemoryFitStartTime(long memoryFitStartTime) {
        this.memoryFitStartTime = memoryFitStartTime;
    }

    public long getMemoryFitEndTime() {
        return memoryFitEndTime;
    }

    public void setMemoryFitEndTime(long memoryFitEndTime) {
        this.memoryFitEndTime = memoryFitEndTime;
    }
}

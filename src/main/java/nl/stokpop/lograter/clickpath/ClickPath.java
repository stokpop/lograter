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
package nl.stokpop.lograter.clickpath;

import net.jcip.annotations.NotThreadSafe;

import java.util.ArrayList;
import java.util.List;

@NotThreadSafe
public class ClickPath {

    public static final String CLICKPATH_SEP = " > ";
    public static final String CLICKPATH_END = " |";

    private long startTimestamp;
	private long endTimestamp;
    private String sessionId;
	private List<String> path;
    private List<Long> durationPerStepInMillis;

	public ClickPath(final long startTimestamp, String step, String sessionId) {
		this.startTimestamp = startTimestamp;
		this.endTimestamp = startTimestamp;
        this.path = new ArrayList<>();
        this.path.add(step);
        this.sessionId = sessionId;
        this.durationPerStepInMillis = new ArrayList<>();
    }
	
	public long getEndTimestamp() {
		return endTimestamp;
	}
	public void setEndTimestamp(long endTimestamp) {
		this.endTimestamp = endTimestamp;
	}
	public long getStartTimestamp() {
		return startTimestamp;
	}
	
	public void addToPath(String step, long timestamp) {
		path.add(step);
        if (path.size() > 1) {
            durationPerStepInMillis.add(timestamp - endTimestamp);
        }
        endTimestamp = timestamp;
	}

	public String getPathAsString() {
		StringBuilder pathAsString = new StringBuilder();
        final int numberOfSteps = path.size();
        int steps = 0;
		for (String step : path) {
            pathAsString.append(step);
            if (++steps < numberOfSteps) {
                pathAsString.append(CLICKPATH_SEP);
            }
            else {
                pathAsString.append(CLICKPATH_END);
            }
		}
		return pathAsString.toString();
	}

    public String getPathAsStringWithDuration() {
        StringBuilder pathAsString = new StringBuilder();
        int i = 0;

        final int numberOfDurations = path.size() - 1;
        for (String step : path) {
            if (i < numberOfDurations) {
                pathAsString.append(step).append(CLICKPATH_SEP);
                pathAsString.append(durationPerStepInMillis.get(i++)).append(CLICKPATH_SEP);
            }
            else {
                pathAsString.append(step).append(CLICKPATH_END);
            }
        }
        return pathAsString.toString();
    }

    public String[] getPath() {
        return path.toArray(new String[0]);
    }

    public Long[] getDurationPerStepInMillis() {
        return durationPerStepInMillis.toArray(new Long[0]);
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public String toString() {
        return "ClickPath duration: " + getSessionDurationInMillis() / 1000 + " seconds. Size: " + path.size() + " SessionId: " + sessionId;
    }

    public long getSessionDurationInMillis() {
        return endTimestamp - startTimestamp;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }
}

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
import nl.stokpop.lograter.counter.SimpleCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@NotThreadSafe
public class InMemoryClickpathCollector implements ClickPathCollector {

    private final Logger log = LoggerFactory.getLogger(InMemoryClickpathCollector.class);

	private final Map<String, SimpleCounter> clickpaths;

    /* store an example session id for analysis in log file */
    private final Map<String, String> exampleSessionIdPerClickpath;

    private final Map<String, Long> avgSessionDurationPerClickpath;

    private final Map<String, Long[]> avgTimeBetweenStepsPerClickpath;

    public InMemoryClickpathCollector() {
        avgTimeBetweenStepsPerClickpath = new HashMap<>();
        avgSessionDurationPerClickpath = new HashMap<>();
        exampleSessionIdPerClickpath = new HashMap<>();
        clickpaths = new HashMap<>();
    }

    public Map<String, SimpleCounter> getClickPaths() {
		return clickpaths;
	}

    public String getExampleSessionIdForClickPath(String clickpath) {
        return exampleSessionIdPerClickpath.get(clickpath);
    }

    public long getAvgSessionDurationForClickPath(String clickpath) {
        return avgSessionDurationPerClickpath.get(clickpath);
    }

    public void addClickPath(ClickPath clickPath) {

	    String pathAsString = clickPath.getPathAsString();

	    if (clickpaths.containsKey(pathAsString)) {
	        clickpaths.get(pathAsString).inc();
	    }
	    else {
	        clickpaths.put(pathAsString, new SimpleCounter(1));
	    }

        final long numberOfClickpathsBeforeAdd = clickpaths.get(pathAsString).getCount() - 1;

        if (!exampleSessionIdPerClickpath.containsKey(pathAsString)) {
            exampleSessionIdPerClickpath.put(pathAsString, clickPath.getSessionId());
        }

        final long sessionDurationInMillis = clickPath.getSessionDurationInMillis();
        if (!avgSessionDurationPerClickpath.containsKey(pathAsString)) {
            avgSessionDurationPerClickpath.put(pathAsString, sessionDurationInMillis);
        }
        else {
            final long avgDuration = avgSessionDurationPerClickpath.get(pathAsString);
            final long newAvgDuration = calculateNewAverage(sessionDurationInMillis, avgDuration, numberOfClickpathsBeforeAdd);
            avgSessionDurationPerClickpath.put(pathAsString, newAvgDuration);
        }

        if (!avgTimeBetweenStepsPerClickpath.containsKey(pathAsString)) {
            avgTimeBetweenStepsPerClickpath.put(pathAsString, clickPath.getDurationPerStepInMillis());
        }
        else {
            final Long[] avgTimeBetweenSteps = avgTimeBetweenStepsPerClickpath.get(pathAsString);
            final Long[] timeBetweenSteps = clickPath.getDurationPerStepInMillis();

            final int length = avgTimeBetweenSteps.length;
            final Long[] newAvgTimeBetweenSteps = new Long[length];

            for (int i = 0; i < length; i++) {
                long newAvgTime = calculateNewAverage(timeBetweenSteps[i], avgTimeBetweenSteps[i], numberOfClickpathsBeforeAdd);
                newAvgTimeBetweenSteps[i] = newAvgTime;
            }
            avgTimeBetweenStepsPerClickpath.put(pathAsString, newAvgTimeBetweenSteps);
        }
	}

    private static long calculateNewAverage(long newValue, long existingAverage, long existingCount) {
        return ((existingAverage * (existingCount)) + newValue) / (existingCount + 1);
    }

    public Long[] getAvgDurationBetweenSteps(String pathAsString) {
        return avgTimeBetweenStepsPerClickpath.get(pathAsString);
    }

    public long getClickPathLength(String pathAsString) {
        return avgTimeBetweenStepsPerClickpath.get(pathAsString).length + 1;
    }

    public String getPathAsStringWithAvgDuration(String pathAsString) {
        final Long[] avgTimeBetweenSteps = avgTimeBetweenStepsPerClickpath.get(pathAsString);
        StringBuilder pathAsStringWithDuration = new StringBuilder();
        String[] path = pathAsString.split(ClickPath.CLICKPATH_SEP);
        int i = 0;
        final int numberOfDurations = path.length - 1;
        for (String step : path) {
            if (i < numberOfDurations) {
                pathAsStringWithDuration.append(step).append(ClickPath.CLICKPATH_SEP);
                pathAsStringWithDuration.append(avgTimeBetweenSteps[i++]).append(ClickPath.CLICKPATH_SEP);
            }
            else {
                pathAsStringWithDuration.append(step);
            }
        }
        return pathAsStringWithDuration.toString();
    }


}

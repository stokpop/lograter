/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.processor.applicationlog;

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.counter.SimpleCounter;
import nl.stokpop.lograter.processor.BasicLogData;
import nl.stokpop.lograter.store.TimeMeasurementStoreInMemory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NotThreadSafe
public class ApplicationLogData extends BasicLogData {
	
	private Map<String, SimpleCounter> fatals = new HashMap<>();
	private Map<String, SimpleCounter> errors = new HashMap<>();
	private Map<String, SimpleCounter> warns = new HashMap<>();
	private Map<String, SimpleCounter> infos = new HashMap<>();
	private Map<String, SimpleCounter> debugs = new HashMap<>();
	private Map<String, SimpleCounter> traces = new HashMap<>();

	private Map<ApplicationsLogDetailsKey, List<ApplicationLogDetails>> detailsMap = new HashMap<>();
	private Map<ApplicationLogDetails, SimpleCounter> countPerLogDetails = new HashMap<>();

	private RequestCounter errorsOverTime = new RequestCounter(CounterKey.of("errorsOverTime"), new TimeMeasurementStoreInMemory());
	private RequestCounter warnsOverTime = new RequestCounter(CounterKey.of("warnsOverTime"), new TimeMeasurementStoreInMemory());

	public void addFatal(String key, long timestamp) {
		errorsOverTime.incRequests(timestamp, 0);
		if (fatals.containsKey(key)) {
			fatals.get(key).inc();
		}
		else {
			fatals.put(key,  new SimpleCounter(1));
		}
	}

	public void addError(String key, long timestamp) {
		errorsOverTime.incRequests(timestamp, 0);
		if (errors.containsKey(key)) {
			errors.get(key).inc();
		}
		else {
			errors.put(key,  new SimpleCounter(1));
		}
	}

	public void addWarn(String key, long timestamp) {
		warnsOverTime.incRequests(timestamp, 0);
		if (warns.containsKey(key)) {
			warns.get(key).inc();
		}
		else {
			warns.put(key,  new SimpleCounter(1));
		}
	}

	public void addInfo(String key) {
		if (infos.containsKey(key)) {
			infos.get(key).inc();
		}
		else {
			infos.put(key,  new SimpleCounter(1));
		}
	}

	public void addTrace(String key) {
		if (traces.containsKey(key)) {
			traces.get(key).inc();
		}
		else {
			traces.put(key,  new SimpleCounter(1));
		}
		
	}

	public void addDebug(String key) {
		if (debugs.containsKey(key)) {
			debugs.get(key).inc();
		}
		else {
			debugs.put(key,  new SimpleCounter(1));
		}
	}
	
	private static long countTotalNrOfHits(Collection<SimpleCounter> values) {
		long total = 0L;
		for (SimpleCounter counter : values) {
			total += counter.getCount();
		}
		return total;
	}

	public long getNrOfFatals() {
		return countTotalNrOfHits(fatals.values());
	}

	public long getNrOfErrors() {
		return countTotalNrOfHits(errors.values());
	}

	public long getNrOfWarns() {
		return countTotalNrOfHits(warns.values());
	}

	public long getNrOfInfos() {
		return countTotalNrOfHits(infos.values());
	}

	public long getNrOfDebugs() {
		return countTotalNrOfHits(debugs.values());
	}

	public long getNrOfTraces() {
		return countTotalNrOfHits(traces.values());
	}

	public Map<String, SimpleCounter> getFatals() {
		return fatals;
	}

	public Map<String, SimpleCounter> getErrors() {
		return errors;
	}

	public Map<String, SimpleCounter> getWarns() {
		return warns;
	}

	public Map<String, SimpleCounter> getInfos() {
		return infos;
	}

	public Map<String, SimpleCounter> getDebugs() {
		return debugs;
	}

	public Map<String, SimpleCounter> getTraces() {
		return traces;
	}

	public void addDetails(ApplicationsLogDetailsKey key, String message, String[] nonLogLines) {
		detailsMap.computeIfAbsent(key, k -> new ArrayList<>());
		List<ApplicationLogDetails> detailsList = detailsMap.get(key);
		if (detailsList.size() <= 20) {
			ApplicationLogDetails details = new ApplicationLogDetails(message, nonLogLines);
			if (!detailsList.contains(details)) {
				detailsList.add(details);
				countPerLogDetails.put(details, new SimpleCounter(1));
			}
			else {
				countPerLogDetails.get(details).inc();
			}
		}
	}

	public List<ApplicationLogDetails> findApplicationLogDetails(ApplicationsLogDetailsKey key) {
		return detailsMap.get(key);
	}

	public Map<ApplicationLogDetails, SimpleCounter> getCountPerLogDetails() {
		return countPerLogDetails;
	}

	public RequestCounter getErrorsOverTime() {
		return errorsOverTime;
	}

	public RequestCounter getWarnsOverTime() {
		return warnsOverTime;
	}

	/**
	 * @return unmodifiable sorted map copy with immutable keys and data.
	 */
	public Map<ApplicationsLogDetailsKey, List<ApplicationLogDetails>> getDetails() {
		Map<ApplicationsLogDetailsKey, List<ApplicationLogDetails>> mapCopy = new HashMap<>();
		List<ApplicationsLogDetailsKey> sortedKeys = new ArrayList<>(detailsMap.keySet());
		Collections.sort(sortedKeys);
		for (ApplicationsLogDetailsKey key : applicationsLogDetailsKeys()) {
			List<ApplicationLogDetails> detailsList = detailsMap.get(key);
			Collections.sort(detailsList);
			mapCopy.put(key, detailsList);
		}
		return Collections.unmodifiableMap(mapCopy);
	}

	public List<ApplicationsLogDetailsKey> applicationsLogDetailsKeys() {
		List<ApplicationsLogDetailsKey> allKeys = new ArrayList<>(detailsMap.keySet());
		Collections.sort(allKeys);
		return allKeys;
	}
	
}
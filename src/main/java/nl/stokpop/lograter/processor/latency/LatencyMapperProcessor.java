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
package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.logentry.LatencyLogEntry;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.linemapper.LineMap;
import nl.stokpop.lograter.util.linemapper.LineMapperCallback;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LatencyMapperProcessor implements Processor<LatencyLogEntry> {

    private static final Logger log = LoggerFactory.getLogger(LatencyMapperProcessor.class);

	private static final String NO_MAPPER = "NO_MAPPER";

    private final RequestCounterStorePair counterStorePair;

    private final LineMapperSection lineMapperSection;

	private final Set<String> reportedNonMatchers = new HashSet<>();
	private final Set<String> reportedMultiMatchers = new HashSet<>();

    // needed to report the used regexp for this mapper key
    private final Map<CounterKey, LineMap> keyToLineMap = new HashMap<>();

    private final LatencyCounterKeyCreator counterKeyCreator;

    private final boolean isIgnoreMultiAndNoMatches;
    private final boolean isDoCountMultipleMapperHits;
    private final boolean isDoCountNoMappersAsOne;

    public LatencyMapperProcessor(
            final RequestCounterStorePair counterStorePair,
            final LineMapperSection lineMapperSection,
            final LatencyCounterKeyCreator keyCreator,
            boolean isDoCountNoMappersAsOne,
            boolean isIgnoreMultiAndNoMatches,
            boolean isDoCountMultipleMapperHits) {
		this.lineMapperSection = lineMapperSection;
        this.counterStorePair = counterStorePair;
        this.counterKeyCreator = keyCreator;
        this.isDoCountNoMappersAsOne = isDoCountNoMappersAsOne;
        this.isIgnoreMultiAndNoMatches = isIgnoreMultiAndNoMatches;
        this.isDoCountMultipleMapperHits = isDoCountMultipleMapperHits;
    }

	@Override
	public void processEntry(final LatencyLogEntry logEntry) {
        updateMappers(logEntry);
	}

	private void updateMappers(final LatencyLogEntry logEntry) {
		
		LineMapperCallback callback = new LineMapperCallback() {
			
			@Override
			public void noMatchFound(String line) {
				if (!reportedNonMatchers.contains(line) && !isIgnoreMultiAndNoMatches) {
					reportedNonMatchers.add(line);
					logNonMatcher(line, reportedNonMatchers.size());
				}
				if (!isIgnoreMultiAndNoMatches) {
                    String baseName = isDoCountNoMappersAsOne ? NO_MAPPER + "-total" : NO_MAPPER + "-" + logEntry.getMessage();
                    CounterKey mapperCounterKey = counterKeyCreator.createCounterKey(logEntry, baseName);
					addToCounterStore(mapperCounterKey, logEntry);
				}
			}
			
			@Override
			public void multiMatchFound(String line, int hits) {
				if (isDoCountMultipleMapperHits) {
					String message = "Multiple matches (" + hits + ") for: " + line;
					if (!reportedMultiMatchers.contains(line)) {
						if (!isIgnoreMultiAndNoMatches) {
							log.info(message);
						}
						reportedMultiMatchers.add(line);
					}
				}
			}
			
			@Override
			public void matchFound(LineMap mapper) {
                CounterKey key = counterKeyCreator.createCounterKey(logEntry, mapper);
				addToCounterStore(key, logEntry);
				keyToLineMap.computeIfAbsent(key, k -> mapper);
			}
		};
		lineMapperSection.updateMappers(logEntry.getMessage(), isDoCountMultipleMapperHits, callback);
	}

    private void logNonMatcher(String line, int nonMatchesCount) {
        if (!counterStorePair.isOverflowing()) {
            log.warn("Total mapper non-matches: {}. No match found for: [{}].", nonMatchesCount, line);
        } else {
            log.debug("Total mapper non-matches: {}. No match found for: [{}].", nonMatchesCount, line);
        }
        if (nonMatchesCount % 1000 == 0) {
            log.warn("Total non-matches found: [{}]", nonMatchesCount);
        }
    }

    private void addToCounterStore(CounterKey key, LatencyLogEntry logEntry) {
		if (logEntry.isSuccess()) {
			counterStorePair.addSuccess(key, logEntry.getTimestamp(), logEntry.getDurationInMillis());
		}
		else {
			counterStorePair.addFailure(key, logEntry.getTimestamp(), logEntry.getDurationInMillis());
		}
	}

	public RequestCounterStore getMappersRequestCounterStoreSuccess() {
		return counterStorePair.getRequestCounterStoreSuccess();
	}

	public RequestCounterStore getMappersRequestCounterStoreFailure() {
		return counterStorePair.getRequestCounterStoreFailure();
	}

    public Map<CounterKey, LineMap> getKeyToLineMap() {
        return Collections.unmodifiableMap(new HashMap<>(keyToLineMap));
    }

}

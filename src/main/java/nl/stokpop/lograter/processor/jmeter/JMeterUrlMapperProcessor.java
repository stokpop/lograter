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
package nl.stokpop.lograter.processor.jmeter;

import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.linemapper.LineMap;
import nl.stokpop.lograter.util.linemapper.LineMapperCallback;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JMeterUrlMapperProcessor implements Processor<JMeterLogEntry> {

    private static final Logger log = LoggerFactory.getLogger(JMeterUrlMapperProcessor.class);

	private static final String NO_MAPPER = "NO_MAPPER";

    private final RequestCounterStorePair counterStorePair;

    private final LineMapperSection lineMapperSection;

	private final Set<String> reportedNonMatchers = new HashSet<>();
	private final Set<String> reportedMultiMatchers = new HashSet<>();

    // needed to report the used regexp for this mapper key
    private final Map<String, LineMap> counterKeyToLineMapMap = new HashMap<>();

    private final JMeterCounterKeyCreator counterKeyCreator;

    private final boolean isIgnoreMultiAndNoMatches;
    private final boolean isDoCountMultipleMapperHits;
    private final boolean isDoCountNoMappersAsOne;

    public JMeterUrlMapperProcessor(
            final RequestCounterStorePair counterStorePair,
            final LineMapperSection lineMapperSection,
            final JMeterCounterKeyCreator keyCreator,
            boolean isDoCountNoMappersAsOne,
            boolean isIgnoreMultiAndNoMatches,
            boolean isDoCountMultipleMapperHits,
            int maxNoMapperCount) {
		this.lineMapperSection = lineMapperSection;
        this.counterStorePair = counterStorePair;
        this.counterKeyCreator = keyCreator;
        this.isDoCountNoMappersAsOne = isDoCountNoMappersAsOne;
        this.isIgnoreMultiAndNoMatches = isIgnoreMultiAndNoMatches;
        this.isDoCountMultipleMapperHits = isDoCountMultipleMapperHits;
    }

	@Override
	public void processEntry(final JMeterLogEntry logEntry) {
        updateMappers(logEntry);
	}

	private void updateMappers(final JMeterLogEntry logEntry) {
		
		LineMapperCallback callback = new LineMapperCallback() {
			
			@Override
			public void noMatchFound(String line) {
				if (!reportedNonMatchers.contains(line)) {
					if (!isIgnoreMultiAndNoMatches) {
                        reportedNonMatchers.add(line);
                        logNonMatcher(line, reportedNonMatchers.size());
                    }
				}
				if (!isIgnoreMultiAndNoMatches) {
					String mapperCounterKey;
                    String baseName = isDoCountNoMappersAsOne ? NO_MAPPER + "-total" : NO_MAPPER + "-" + logEntry.getUrl();
                    mapperCounterKey = counterKeyCreator.createCounterKey(logEntry, baseName);
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
                String mapperCounterKey = counterKeyCreator.createCounterKey(logEntry, mapper);
				addToCounterStore(mapperCounterKey, logEntry);
				if (!counterKeyToLineMapMap.containsKey(mapperCounterKey)) {
                    counterKeyToLineMapMap.put(mapperCounterKey, mapper);
                }
			}
		};			
		
		lineMapperSection.updateMappers(logEntry.getUrl(), isDoCountMultipleMapperHits, callback);
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

    private void addToCounterStore(String counterKey, JMeterLogEntry logEntry) {
		if (logEntry.isSuccess()) {
			counterStorePair.addSuccess(counterKey, logEntry.getTimestamp(), logEntry.getDurationInMillis());
		}
		else {
			counterStorePair.addFailure(counterKey, logEntry.getTimestamp(), logEntry.getDurationInMillis());
		}
	}

	public RequestCounterStore getMappersRequestCounterStoreSuccess() {
		return counterStorePair.getRequestCounterStoreSuccess();
	}

	public RequestCounterStore getMappersRequestCounterStoreFailure() {
		return counterStorePair.getRequestCounterStoreFailure();
	}

    public Map<String, LineMap> getCounterKeyToLineMapMap() {
        return Collections.unmodifiableMap(new HashMap<>(counterKeyToLineMapMap));
    }

}

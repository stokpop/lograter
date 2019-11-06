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
package nl.stokpop.lograter.processor.accesslog;

import nl.stokpop.lograter.logentry.AccessLogEntry;
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

public class AccessLogUrlMapperProcessor implements Processor<AccessLogEntry> {

    private static final Logger log = LoggerFactory.getLogger(AccessLogUrlMapperProcessor.class);

	private static final String NO_MAPPER = "NO_MAPPER";
	private static final String NO_MAPPER_OVERFLOW = "NO_MAPPER_OVERFLOW";
    private static final String LOG_MSG_NON_MATCHES_COUNT = "Total mapper non-matches: {}. No match found for: [{}].";

    private final RequestCounterStorePair counterStorePair;

    private final LineMapperSection lineMapperSection;

	private final Set<String> reportedNonMatchers = new HashSet<>();
	private final Set<String> reportedMultiMatchers = new HashSet<>();

    // needed to report the used regexp for this mapper key
    private final Map<String, LineMap> counterKeyToLineMapMap = new HashMap<>();

    private final AccessLogCounterKeyCreator counterKeyCreator;

    private final boolean isIgnoreMultiAndNoMatches;
    private final boolean isDoCountMultipleMapperHits;
    private final boolean isDoCountNoMappersAsOne;
    private final int maxNoMapperCount;

    public AccessLogUrlMapperProcessor(
            final RequestCounterStorePair counterStorePair,
            final LineMapperSection lineMapperSection,
            final AccessLogCounterKeyCreator keyCreator,
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
        this.maxNoMapperCount = maxNoMapperCount;
    }

	@Override
	public void processEntry(final AccessLogEntry logEntry) {
        updateMappers(logEntry);
	}

	private void updateMappers(final AccessLogEntry logEntry) {
		
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
					if (reportedNonMatchers.size() > maxNoMapperCount) {
						mapperCounterKey = NO_MAPPER_OVERFLOW;
					}
					else {
						String baseName = isDoCountNoMappersAsOne ? NO_MAPPER + "-total" : NO_MAPPER + "-" + logEntry.getUrl();
						mapperCounterKey = counterKeyCreator.createCounterKey(logEntry, baseName);
					}
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
        if (nonMatchesCount <= maxNoMapperCount) {
            log.warn(LOG_MSG_NON_MATCHES_COUNT, nonMatchesCount, line);
        } else {
            log.debug(LOG_MSG_NON_MATCHES_COUNT, nonMatchesCount, line);
        }
        if (nonMatchesCount % 1000 == 0) {
            log.warn("Total warnings about 'Total non-matches': [{}]", nonMatchesCount);
        }
    }

    private void addToCounterStore(String counterKey, AccessLogEntry logEntry) {
		if (logEntry.isHttpError()) {
			counterStorePair.addFailure(counterKey, logEntry.getTimestamp(), logEntry.getDurationInMillis());
		}
		else {
			counterStorePair.addSuccess(counterKey, logEntry.getTimestamp(), logEntry.getDurationInMillis());

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

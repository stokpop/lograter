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
package nl.stokpop.lograter.processor.accesslog;

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.counter.CounterKey;
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
import java.util.concurrent.atomic.AtomicLong;

@NotThreadSafe
public class AccessLogUrlMapperProcessor implements Processor<AccessLogEntry> {

    private static final Logger log = LoggerFactory.getLogger(AccessLogUrlMapperProcessor.class);

	private static final String NO_MAPPER = "NO_MAPPER";

    private final RequestCounterStorePair counterStorePair;

    private final LineMapperSection lineMapperSection;

	private final Set<String> reportedNonMatchers = new HashSet<>();
	private final AtomicLong nonMatchersCount = new AtomicLong();
	private final Set<String> reportedMultiMatchers = new HashSet<>();

    // needed to report the used regexp for this mapper key
    private final Map<CounterKey, LineMap> keyToLineMap = new HashMap<>();

    private final AccessLogCounterKeyCreator counterKeyCreator;

    private final boolean isIgnoreMultiAndNoMatches;
    private final boolean isDoCountMultipleMapperHits;
    private final boolean isDoCountNoMappersAsOne;

    public AccessLogUrlMapperProcessor(
            final RequestCounterStorePair counterStorePair,
            final LineMapperSection lineMapperSection,
            final AccessLogCounterKeyCreator keyCreator,
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
	public void processEntry(final AccessLogEntry logEntry) {
        updateMappers(logEntry);
	}

	private void updateMappers(final AccessLogEntry logEntry) {
		
		LineMapperCallback callback = new LineMapperCallback() {
			@Override
			public void noMatchFound(String line) {
				if (!reportedNonMatchers.contains(line) && !isIgnoreMultiAndNoMatches && !counterStorePair.isOverflowing()) {
					reportedNonMatchers.add(line);
					logNonMatcher(line, nonMatchersCount.incrementAndGet());
				}
				if (!isIgnoreMultiAndNoMatches) {
                    String baseName = isDoCountNoMappersAsOne ? NO_MAPPER + "-total" : NO_MAPPER + "-" + logEntry.getUrl();
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

		lineMapperSection.updateMappers(logEntry.getUrl(), isDoCountMultipleMapperHits, callback);
	}

    private void logNonMatcher(String line, long nonMatchesCount) {

        String name = lineMapperSection.getName();

        if (!counterStorePair.isOverflowing()) {
            log.warn("Total mapper non-matches ({}): [{}] no match found for: [{}].", name, nonMatchesCount, line);
        } else {
            log.debug("Total mapper non-matches ({}): [{}] no match found for: [{}].", name, nonMatchesCount, line);
        }
        if (nonMatchesCount % 1000 == 0) {
            log.warn("Total non-matches found ({}): [{}]", name, nonMatchesCount);
        }
    }

    private void addToCounterStore(CounterKey counterKey, AccessLogEntry logEntry) {
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

    public Map<CounterKey, LineMap> getKeyToLineMap() {
        return Collections.unmodifiableMap(new HashMap<>(keyToLineMap));
    }

    @Override
    public String toString() {
        return "AccessLogUrlMapperProcessor{" + "counterStorePair=" + counterStorePair +
                ", lineMapperSection=" + lineMapperSection +
                ", nonMatchersCount=" + nonMatchersCount +
                ", isIgnoreMultiAndNoMatches=" + isIgnoreMultiAndNoMatches +
                ", isDoCountMultipleMapperHits=" + isDoCountMultipleMapperHits +
                ", isDoCountNoMappersAsOne=" + isDoCountNoMappersAsOne +
                '}';
    }
}

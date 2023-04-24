/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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

import nl.stokpop.lograter.clickpath.ClickPathAnalyser;
import nl.stokpop.lograter.logentry.LatencyLogEntry;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.linemapper.LineMap;
import nl.stokpop.lograter.util.linemapper.LineMapperCallback;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LatencyLogClickPathProcessor implements Processor<LatencyLogEntry> {

	private final static Logger log = LoggerFactory.getLogger(LatencyLogClickPathProcessor.class);

	private final ClickPathAnalyser clickPathAnalyser;
	private final LineMapperSection lineMapper;
	private final String sessionField;
	private final List<String> counterFields;
	private final int shortCodeLength;

    private final Set<String> reportedNonMatchers = new HashSet<>();
    private final Set<String> reportedMultiMatchers = new HashSet<>();


	public LatencyLogClickPathProcessor(ClickPathAnalyser clickPathAnalyser, LineMapperSection lineMapper, String sessionField, List<String> counterFields) {
		this(clickPathAnalyser, lineMapper, sessionField, counterFields, 3);
	}

    public LatencyLogClickPathProcessor(ClickPathAnalyser clickPathAnalyser, LineMapperSection lineMapper, String sessionField, List<String> counterFields, int shortCodeLength) {
        this.clickPathAnalyser = clickPathAnalyser;
        this.lineMapper = lineMapper;
        this.sessionField = sessionField;
        this.counterFields = counterFields;
        this.shortCodeLength = shortCodeLength;
    }

    @Override
	public void processEntry(final LatencyLogEntry entry) {

		LineMapperCallback callback = new LineMapperCallback() {
			@Override
			public void noMatchFound(String line) {
				String shortCode = shortCode(line);
				if (!reportedNonMatchers.contains(line)) {
					log.info("No match found for: {} Using shortCode in clickpath: {}", line, shortCode);
					reportedNonMatchers.add(line);
				}
				String sessionId = entry.getField(getSessionField());
				getClickPathAnalyser().addLineEntry(sessionId, shortCode, entry.getTimestamp());
			}

			private String shortCode(String line) {

				int hashCode = Math.abs(line.hashCode());

				String[] lineParts = line.split("/");
				StringBuilder shortCode = new StringBuilder();
				for (String linePart : lineParts) {
					shortCode.append(linePart.length() > shortCodeLength ? linePart.substring(0, shortCodeLength) : linePart);
					shortCode.append("/");
				}

				shortCode.append("#").append(hashCode);
				return shortCode.toString();
			}

			@Override
			public void multiMatchFound(String line, int hits) {
				log.info("Multiple matches ({}) for: {} Ignoring in clickpath!", hits, line);
			}

			@Override
			public void matchFound(LineMap mapper) {
				String sessionId = entry.getField(getSessionField());
				getClickPathAnalyser().addLineEntry(sessionId, mapper.getName(), entry.getTimestamp());
			}
		};

		String operation = counterFields.stream().map(entry::getField).collect(Collectors.joining("-"));
		lineMapper.updateMappers(operation, false, callback);
	}
	
	public ClickPathAnalyser getClickPathAnalyser() {
		return clickPathAnalyser;
	}

	public String getSessionField() {
		return sessionField;
	}

}

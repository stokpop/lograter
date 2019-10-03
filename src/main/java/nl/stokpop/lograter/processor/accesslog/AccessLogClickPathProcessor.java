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

import nl.stokpop.lograter.clickpath.ClickPathAnalyser;
import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.linemapper.LineMap;
import nl.stokpop.lograter.util.linemapper.LineMapperCallback;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class AccessLogClickPathProcessor implements Processor<AccessLogEntry> {
	
	private final static Logger log = LoggerFactory.getLogger(AccessLogClickPathProcessor.class);

	private ClickPathAnalyser clickPathAnalyser;
	private LineMapperSection lineMapper;
	
	Set<String> reportedNonMatchers = new HashSet<>();
	Set<String> reportedMultiMatchers = new HashSet<>();

	public AccessLogClickPathProcessor(ClickPathAnalyser clickPathAnalyser, LineMapperSection lineMapper) {
		this.clickPathAnalyser = clickPathAnalyser;
		this.lineMapper = lineMapper;
	}

	@Override
	public void processEntry(final AccessLogEntry entry) {
		
		LineMapperCallback callback = new LineMapperCallback() {
			
			@Override
			public void noMatchFound(String line) {
				int hashCode = line.hashCode();
				if (!reportedNonMatchers.contains(line)) {
					log.info("No match found for: {} Using line hashCode in clickpath: {}", line, hashCode);
					reportedNonMatchers.add(line);
				}					
				getClickPathAnalyser().addLineEntry(entry.getSessionId(), Integer.toString(hashCode), entry.getTimestamp());
			}
			
			@Override
			public void multiMatchFound(String line, int hits) {
				log.info("Multiple matches ({}) for: {} Ignoring in clickpath!", hits, line);
			}
			
			@Override
			public void matchFound(LineMap mapper) {
				getClickPathAnalyser().addLineEntry(entry.getSessionId(), mapper.getName(), entry.getTimestamp());
			}
		};			
		
		lineMapper.updateMappers(entry.getUrl(), false, callback);
	}
	
	public ClickPathAnalyser getClickPathAnalyser() {
		return clickPathAnalyser;
	}

}

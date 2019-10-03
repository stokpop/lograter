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
package nl.stokpop.lograter.parser;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.parser.line.LogFormatParser;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AccessLogParser implements LogFileParser<AccessLogEntry> {
	
	private static final Logger log = LoggerFactory.getLogger(AccessLogParser.class);
	
	private final LogFormatParser<AccessLogEntry> lineParser;
	private final List<Processor<AccessLogEntry>> processors = new ArrayList<>();
	private final TimePeriod filterTimePeriod;

	public AccessLogParser(LogFormatParser<AccessLogEntry> lineParser, TimePeriod filterPeriod) {
		super();
		this.lineParser = lineParser;
		this.filterTimePeriod = filterPeriod;
	}

	@Override
	public void addLogLine(final String filename, final String logLine)  {
		
		if (logLine.isEmpty()) {
//			this.nonlogline++;
			return;
		}

		AccessLogEntry entry = this.lineParser.parseLogLine(logLine);

        // sanity check: is at least the url present?
        if (entry.getUrl() == null) {
            throw new LogRaterException("Expected at least a url to be parsed for this line, check the logpattern for \"%r\" (mind % escaping in dos cmd!): [" + entry.getLogline() + "]");
        }
		
		log.debug("parsed line: {}", entry);
		long timestamp = entry.getTimestamp();
		
		if (!filterTimePeriod.isWithinTimePeriod(timestamp)) {
//			data.incFilteredLines();
			return;
		}
		
		for (Processor<AccessLogEntry> processor : processors) {
			processor.processEntry(entry);
		}		

	}

	public void addProcessor(Processor<AccessLogEntry> processor) {
		this.processors.add(processor);
	}

}

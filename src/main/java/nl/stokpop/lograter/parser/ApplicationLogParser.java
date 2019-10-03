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

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.logentry.LogbackLogEntry;
import nl.stokpop.lograter.parser.line.LogbackParser;
import nl.stokpop.lograter.processor.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@NotThreadSafe
public class ApplicationLogParser implements LogFileParser<LogbackLogEntry> {

    private Logger log = LoggerFactory.getLogger(ApplicationLogParser.class);

    private static final int MAX_NON_LOGLINES = 999;

	private LogbackParser<LogbackLogEntry> logbackParser;
	private List<String> nonLogLines = new ArrayList<>();
	private LogbackLogEntry previousEntry;
	
	private List<Processor<LogbackLogEntry>> processors = new ArrayList<>();

	public ApplicationLogParser(LogbackParser<LogbackLogEntry> parser) {
		super();
		this.logbackParser = parser;
	}
	
	public void addProcessor(Processor<LogbackLogEntry> processor) {
		processors.add(processor);
	}

	public void addLogLine(final String logFilename, final String logLine) {

		LogbackLogEntry entry;
		try {
			 entry = logbackParser.parseLogLine(logLine);
		} catch (RuntimeException exception) {
			log.debug("NON-LOGLINE: '{}' reason: [{}]", logLine, exception.getMessage());
			log.trace("STACKTRACE: ", exception);
            if (nonLogLines.size() < MAX_NON_LOGLINES) {
			    nonLogLines.add(logLine);
            }
            else {
                log.error(String.format("More than [%d] non-loglines found in [%s]. " +
                        "Check if logpattern is OK. See error below. " +
                        "Clearing non-loglines collected so far. " +
                        "Total non-loglines counter will be missing 500 entries.",
                        MAX_NON_LOGLINES, logFilename));
                log.error("NON-LOGLINE: {} reason: {}", logLine, exception);
                nonLogLines.clear();
            }
			return;
		}
		
		entry.setLogline(logLine);
		entry.setLogFilename(logFilename);
		
		if (nonLogLines.size() > 0) {
			previousEntry.addNonLogLinesCopy(nonLogLines);
			nonLogLines.clear();
		}

		if (previousEntry != null) {
			log.debug("new entry: {}", previousEntry);
			for (Processor<LogbackLogEntry> processor : processors) {
				processor.processEntry(previousEntry);
			}
		}

		previousEntry = entry;
	}

	public void processLastEntry() {
		if (previousEntry != null) {
			log.debug("new entry: {}", previousEntry);
			for (Processor<LogbackLogEntry> processor : processors) {
				processor.processEntry(previousEntry);
			}
		}
	}
}

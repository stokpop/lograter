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
package nl.stokpop.lograter.parser;

import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.logentry.IisLogEntry;
import nl.stokpop.lograter.parser.line.IisLogFormatParser;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.LogRaterUtils;
import nl.stokpop.lograter.util.SessionIdParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class IisLogParser implements LogFileParser<AccessLogEntry> {

    private final static Logger log = LoggerFactory.getLogger(IisLogParser.class);

	private final IisLogFormatParser lineParser;
	private final List<Processor<AccessLogEntry>> processors = new ArrayList<>();
	private final SessionIdParser sessionIdParser;

	public IisLogParser(IisLogFormatParser lineParser, SessionIdParser sessionIdParser) {
		super();
		this.lineParser = lineParser;
		this.sessionIdParser = sessionIdParser;
	}

    public IisLogParser(IisLogFormatParser iisLogFormatParser) {
        this(iisLogFormatParser, SessionIdParser.NO_SESSION_ID_PARSER);
    }

    @Override
	public void addLogLine(final String filename, final String logLine) {

		if (logLine.isEmpty() || logLine.startsWith("#")) {
			log.debug("Non log line: {}", logLine);
			return;
		}

		IisLogEntry entry = this.lineParser.parseLogLine(logLine);

		String sessionId = sessionIdParser.parseSessionId(entry);

		if (!LogRaterUtils.isEmpty(entry.getSessionId())) {
			log.warn("SessionId is already present. This is not expected! Entry: {}", entry);
		}
		else {
			entry.setSessionId(sessionId);
		}

		for (Processor<AccessLogEntry> processor : processors) {
			processor.processEntry(entry);
		}

	}

	public void addProcessor(Processor<AccessLogEntry> processor) {
		this.processors.add(processor);
	}

}

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
package nl.stokpop.lograter.parser.line;


import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.logentry.IisLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class IisLogFormatParser {
	
	private static final String FIELDS = "#Fields: ";

	private static final Logger log = LoggerFactory.getLogger(IisLogFormatParser.class.getName());
	
	private final List<LogbackElement> elements;
	private final Map<String, LogEntryMapper<IisLogEntry>> mappers;
	
	public IisLogFormatParser(List<LogbackElement> elements, Map<String, LogEntryMapper<IisLogEntry>> mappers) {
		this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
		this.mappers = Collections.unmodifiableMap(new HashMap<>(mappers));
	}

    public static IisLogFormatParser createIisLogFormatParser(String pattern) {
        List<LogbackElement> elements = parse(pattern);
        Map<String, LogEntryMapper<IisLogEntry>> mappers = IisLogEntry.initializeMappers(elements);
        return new IisLogFormatParser(elements, mappers);
    }

    public IisLogEntry parseLogLine(String logline) {
		
		IisLogEntry entry = new IisLogEntry();

        entry.setLogline(logline);

		int locationInLine = 0;
		LogbackDirective var = null;

		for (LogbackElement element : elements) {
			if (element instanceof LogbackLiteral) {
				String search = ((LogbackLiteral) element).getLiteral();
				// if search length is zero, and it is not the first element (var == null), there are two variables without separator, take all
				boolean isLastParsableEntry = search.length() == 0 && var != null;
				int idx =  isLastParsableEntry ? logline.length() : logline.indexOf(search, locationInLine);
				if (var != null) {
					String directive = var.getDirective();
					log.trace("lookup: {} var: {}", directive, var);
					String value;
					try {
						value = logline.substring(locationInLine, idx);
					} catch (StringIndexOutOfBoundsException e) {
						throw new LogRaterException("Problem parsing log line searching '" + search + "' for " + var + " in logline " + logline, e);
					}

                    entry.addField(directive, value);

                    LogEntryMapper<IisLogEntry> mapper = mappers.get(directive);
                    if (mapper != null) {
                        mapper.writeToLogEntry(value, var.getVariable(), entry);
                    }
				}
				locationInLine = idx + search.length();
				if (isLastParsableEntry) {
					break;
				}
            }
			else if (element instanceof LogbackDirective) {
				var = (LogbackDirective) element;
				// if newline is found, stop the loop
				boolean isLastParsableEntry = "n".equals(var.getDirective());
				if (isLastParsableEntry) {
					break;
				}
			}
			else {
				throw new LogRaterException("Unknown element type in log back elements: " + element);
			}
		}
		return entry;
	}

	public static List<LogbackElement> parse(String pattern) {
		List<LogbackElement> elements = new ArrayList<>();
		
		pattern = pattern.trim();
		
		// skip #Fields if present
		if (pattern.startsWith(FIELDS)) {
			pattern = pattern.substring(FIELDS.length());
		}
		
		StringBuilder literal = new StringBuilder();
		char[] iisPattern = pattern.toCharArray();

        for (char c : iisPattern) {
            if (c == ' ') {
                LogbackDirective entry = LogbackDirective.from(literal.toString());
                elements.add(entry);
                literal.setLength(0);
                elements.add(new LogbackLiteral(" "));
                // skip ' ' itself
                continue;
            }
            literal.append(c);
        }
		// final element
		if (literal.length() > 0) {
            LogbackDirective entry = LogbackDirective.from(literal.toString());
			elements.add(entry);
			// make sure the final var is being processed, merge a final literal
			elements.add(new LogbackLiteral(""));
		}
		log.debug("Elements: {} parsed from pattern: {} ", elements, pattern);
		return elements;
	}

}

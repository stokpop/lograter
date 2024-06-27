/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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
import nl.stokpop.lograter.logentry.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NginxLogFormatParser<T extends LogEntry> implements LogFormatParser<T> {

	private static final Logger log = LoggerFactory.getLogger(NginxLogFormatParser.class.getName());

	private final List<LogbackElement> elements;
	private final Map<String, LogEntryMapper<T>> mappers;

	private final LogEntryFactory<T> logEntryFactory;

	public NginxLogFormatParser(List<LogbackElement> elements, Map<String, LogEntryMapper<T>> mappers, LogEntryFactory<T> logEntryFactory) {
		this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
		this.mappers = Collections.unmodifiableMap(new HashMap<>(mappers));
		this.logEntryFactory = logEntryFactory;
	}

	@Override
	public T parseLogLine(String logline) {
		
		T entry = logEntryFactory.newInstance();

        entry.setLogline(logline);

		int locationInLine = 0;
		LogbackDirective var = null;

		for (LogbackElement element : elements) {
			if (element instanceof LogbackLiteral) {
				String search = ((LogbackLiteral) element).getLiteral();
				// if search length is zero, and it is not the first element (var == null), there are two variables without separator, take all
				boolean isLastParsableEntry = search.length() == 0 && var != null;
				int indexOfSearch =  isLastParsableEntry ? logline.length() : logline.indexOf(search, locationInLine);
				if (var != null) {
					log.trace("directive var: {}", var);
					String directive = var.getDirective();
					String value;
					try {
						value = logline.substring(locationInLine, indexOfSearch);
					} catch (StringIndexOutOfBoundsException e) {
						throw new LogRaterException("Problem parsing log line searching '" + search + "' for " + var + " in logline " + logline, e);
					}
                    entry.addField(directive, value);
					LogEntryMapper<T> mapper = mappers.get(directive);
					if (mapper != null) {
						mapper.writeToLogEntry(value, var.getVariable(), entry);
					}
				}
				locationInLine = indexOfSearch + search.length();
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

		boolean isDirective = false;
		StringBuilder currentText = new StringBuilder();
		char[] lbp = pattern.toCharArray();
		for (int i = 0; i < lbp.length; i++) {
			char c = lbp[i];
			if (!isDirective && c == '$') {
                // if double $$ then it is not a variable, but a single literal %!
                if (lbp[i + 1] == '$') {
                    currentText.append('$');
                    i++;
                    continue;
                }
				isDirective = true;
				elements.add(new LogbackLiteral(currentText.toString()));
				currentText.setLength(0);
				// skip $ itself
				continue;
			}
			if (isDirective && !(Character.isLetter(c) || c == '-' || c == '_')) {
				isDirective = false;
                LogbackDirective var = LogbackDirective.from(currentText.toString());
                elements.add(var);
				currentText.setLength(0);
			}
			currentText.append(c);
		}
		// final element
        elements.add(new LogbackLiteral(currentText.toString()));
		log.debug("Elements: {} parsed from pattern: {} ", elements, pattern);
		return elements;
	}

}

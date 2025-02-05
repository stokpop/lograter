/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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

public class ApacheLogFormatParser<T extends LogEntry> implements LogFormatParser<T> {

	private static final Logger log = LoggerFactory.getLogger(ApacheLogFormatParser.class.getName());

	private final List<LogbackElement> elements;
	private final Map<String, LogEntryMapper<T>> mappers;

	private final LogEntryFactory<T> logEntryFactory;

	public ApacheLogFormatParser(List<LogbackElement> elements, Map<String, LogEntryMapper<T>> mappers, LogEntryFactory<T> logEntryFactory) {
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
                    if (directive.equals("t") && var.getVariable() == null) {
                        // default it is a timestamp, which is surrounded by [ and ] and may contain spaces.
                        // NOTE: is not surrounded by [] with custom formats
                        indexOfSearch = logline.indexOf("]" + search, locationInLine) + 1;
                    }
					String value;
					try {
						value = logline.substring(locationInLine, indexOfSearch);
					} catch (StringIndexOutOfBoundsException e) {
						throw new LogRaterException("Problem parsing log line searching '" + search + "' for " + var + " in logline " + logline, e);
					}
                    // use variable name for i, x, C, o, n, e directives (theoretically can result in name clash)
                    String fieldName = directive;
					// is x in use? cannot find it on the internet?
                    if ("i".equals(directive) || "x".equals(directive) || "X".equals(directive) || "C".equals(directive) || "o".equals(directive) || "n".equals(directive) || "e".equals(directive) ) {
                        fieldName = var.getVariable();
                    }
                    entry.addField(fieldName, value);
					LogEntryMapper<T> mapper = mappers.get(directive);
					if (mapper != null) {
						mapper.writeToLogEntry(value, var.getVariable(), entry);
					}
					// done processing var
					var = null;
				}
				locationInLine = indexOfSearch + search.length();
				if (isLastParsableEntry) {
					break;
				}
            }
			else if (element instanceof LogbackDirective) {
				var = (LogbackDirective) element;
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
		boolean isVariable = false;
        boolean wasDirective = false;
        // the abc part of %X{abc} or %{abc}X
		String variable = null;
		// the X part of %X{abc} or %{abc}X
		String directiveLetter = null;
		StringBuilder currentText = new StringBuilder();
		char[] lbp = pattern.toCharArray();
		for (int i = 0; i < lbp.length; i++) {
			char c = lbp[i];
			if (wasDirective && (Character.isLetter(c) || directiveLetter != null)) {
				if (directiveLetter == null) {
					directiveLetter = String.valueOf(c);
				}
				else {
					currentText.append(c);
				}
				wasDirective = false;
				// special case for %{msec_frac}t and %{usec_frac}t
				if ("t".equals(directiveLetter) && ("msec_frac".equals(variable) || "usec_frac".equals(variable))) {
					elements.add(LogbackDirective.from(variable));
				}
				else {
					elements.add(LogbackDirective.from(directiveLetter, variable));
				}
				variable = null;
				directiveLetter = null;
				continue;
			}
			if (!isDirective && c == '%') {
                // if double %% then it is not a variable, but a single literal %!
                if (lbp[i + 1] == '%') {
                    currentText.append('%');
                    i++;
                    continue;
                }
				isDirective = true;
				elements.add(new LogbackLiteral(currentText.toString()));
				currentText.setLength(0);
				// skip % itself
				continue;
			}
			if (isDirective && (c == '{' || c == '<' || c == '>')) {
				if (c == '{') isVariable = true;
				// save the character between % and {, e.g. X in %X{test}
				if (currentText.length() == 1) {
					directiveLetter = currentText.toString();
					currentText.setLength(0);
				}
				else if (currentText.length() > 1) {
					throw new LogRaterException("Unexpected directive, should be just one letter: " + currentText);
				}
                // skip this character in variable
				continue;
			}
			if (isDirective && c == '}') {
                // end of variable
                isVariable = false;
				wasDirective = true;
				isDirective = false;
				variable = currentText.toString();
				currentText.setLength(0);
				continue;
			}
			if (!isVariable && isDirective && !(Character.isLetter(c) || c == '-' || c == '_')) {
				isDirective = false;

				parseDirective(elements, currentText.toString());
				currentText.setLength(0);
			}
			currentText.append(c);
		}
		// final element
		if (isDirective || wasDirective) {
			parseDirective(elements, currentText.toString());
			// last element is empty after final variable
            elements.add(new LogbackLiteral(""));
		}
		else {
			elements.add(new LogbackLiteral(currentText.toString()));
		}
		log.debug("Elements: {} parsed from pattern: {} ", elements, pattern);
		return elements;
	}

	private static void parseDirective(List<LogbackElement> elements, String directive) {
	    if (directive.length() > 1) {
			// we currently only have single character directives so rest is literal
			elements.add(LogbackDirective.from(directive.substring(0, 1)));
			elements.add(new LogbackLiteral(directive.substring(1)));
		} else if (directive.length() == 1) {
			elements.add(LogbackDirective.from(directive.substring(0, 1)));
		} else {
	        throw new LogRaterException("Unexpected empty directive at " + elements);
        }
	}

}

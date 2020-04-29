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
package nl.stokpop.lograter.parser.line;


import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.logentry.LogbackLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogbackParser<T extends LogbackLogEntry> {
	
	private static final Logger log = LoggerFactory.getLogger(LogbackParser.class.getName());
	
	private List<LogbackElement> elements;
	private Map<String, LogEntryMapper<T>> mappers;
	
	// to create new instance
	private Class<T> clazzOfT;

	public LogbackParser(List<LogbackElement> elements, Map<String, LogEntryMapper<T>> mappers, Class<T> clazzOfT) {
		this.elements = elements;
		this.mappers = mappers;
		this.clazzOfT = clazzOfT;
	}

    public static LogbackParser<LogbackLogEntry> createLogbackParser(String logbackpattern) {
        List<LogbackElement> elements = parse(logbackpattern);
        Map<String, LogEntryMapper<LogbackLogEntry>> mappers = LogbackLogEntry.initializeLogBackMappers(elements);
        return new LogbackParser<>(elements, mappers, LogbackLogEntry.class);
    }

    public T parseLogLine(String logline) {
		
		logline = logline.trim();
		
		T entry = newInstanceOfT();
		
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
					if (isXorMdcField(var)) {
						directive = var.getVariable();
					}
					log.trace("lookup: {} var: {}", directive, var);
					String value;
					try {
						value = logline.substring(locationInLine, idx);
					} catch (StringIndexOutOfBoundsException e) {
						if ("m".equals(directive) || "msg".equals(directive)) {
							// assume a new line in a message(!)
							value = logline.substring(locationInLine);
						}
						else {
							throw new LogRaterException("Cannot find search term: '" + search + "' for '" + var + "' on location " + locationInLine + " in logline " + logline, e);
						}
					}
					// make sure spaces before and after value are removed
					value = value.trim();
					if (directive == null) {
						// this is a %X or %mdc without parameter, so a complete dump of all mdc values is expected
						// in comma separated style.
						if (value.length() > 0) {
							String[] nameValuePairs = value.split(",");
							for (String nameValuePair : nameValuePairs) {
								// get the part before and after the first = sign, all other = are in second part
								String[] nameValueTuple = nameValuePair.split("=", 2);
								if (nameValueTuple.length != 2) {
									throw new LogRaterException("Parsing MDC dump failed, comma separated \"name=value\" pair expected, but got: [" + nameValuePair + "] for [" + var + "] on location [" + locationInLine + "] in logline [" + logline + "]");
								}
								entry.addCustomField(nameValueTuple[0].trim(), nameValueTuple[1].trim());
							}
						}
						else {
							log.trace("Empty mdc dump found, not adding any field to log entry.");
						}
					}
					else {
					    if (isXorMdcField(var)) {
                            entry.addCustomField(directive, value);
                        }
					    else {
                            entry.addField(directive, value);
                        }
						LogEntryMapper<T> mapper = mappers.get(directive);
						if (mapper != null) {
							mapper.writeToLogEntry(value, var.getVariable(), entry);
						}
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

    public boolean isXorMdcField(LogbackDirective logbackDirective) {
        return "X".equals(logbackDirective.getDirective()) || "mdc".equals(logbackDirective.getDirective());
    }

    private T newInstanceOfT() {
		try {
			return clazzOfT.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			throw new LogRaterException("Cannot instantiate class: " + clazzOfT.getName(), e);
		}
    }

	public static List<LogbackElement> parse(String logbackpattern) {
		List<LogbackElement> elements = new ArrayList<>();
		
		boolean isVariable = false;
		boolean isParameter = false;
		StringBuilder literal = new StringBuilder();
		char[] lbp = logbackpattern.toCharArray();
        for (char c : lbp) {
            if (c == '%') {
                if (isVariable) {
                    // two variables in a row, close previous one
                    LogbackDirective var = LogbackDirective.from(literal.toString());
                    elements.add(var);
                    literal.setLength(0);
                }
                isVariable = true;
                elements.add(new LogbackLiteral(literal.toString()));
                literal.setLength(0);
                // skip % itself
                continue;
            }
            if (isVariable && c == '{') {
                isParameter = true;
                isVariable = false;
                LogbackDirective var = LogbackDirective.from(literal.toString());
                elements.add(var);
                literal.setLength(0);
                // skip this character
                continue;
            }
            if (isParameter && c == '}') {
                isParameter = false;
                ((LogbackDirective) elements.get(elements.size() - 1)).setVariable(literal.toString());
                literal.setLength(0);
                continue;
            }
            // ignore format modifiers, such as: %-20.30logger
            if (isVariable && literal.length() == 0 && (Character.isDigit(c) || c == '-' || c == '.')) {
                // skip the modifier before the variable name
                continue;
            }
            else if (isVariable && !Character.isLetter(c)) {
                isVariable = false;
                LogbackDirective var = LogbackDirective.from(literal.toString());
                elements.add(var);
                literal.setLength(0);
            }
            literal.append(c);
        }
		// final element
		if (isVariable) {
			LogbackDirective var = LogbackDirective.from(literal.toString());
			elements.add(var);
		}
		else {
			elements.add(new LogbackLiteral(literal.toString()));
		}
		return elements;
	}

}

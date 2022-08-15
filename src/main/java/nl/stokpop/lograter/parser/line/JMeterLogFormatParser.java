/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
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
import nl.stokpop.lograter.processor.jmeter.JMeterLogEntry;
import nl.stokpop.lograter.processor.jmeter.JMeterLogLineType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JMeterLogFormatParser {

    private static final Logger log = LoggerFactory.getLogger(IisLogFormatParser.class.getName());

    private static final String DOUBLE_QUOTE = "\"";
    private static final char DOUBLE_QUOTE_CHAR = DOUBLE_QUOTE.charAt(0);

    private final Map<String, LogEntryMapper<JMeterLogEntry>> mappers;
    private final List<LogbackElement> elements;

    public JMeterLogFormatParser(final List<LogbackElement> elements, final Map<String, LogEntryMapper<JMeterLogEntry>> mappers) {
        this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
        this.mappers = Collections.unmodifiableMap(new HashMap<>(mappers));
    }

    public static Map<String, LogEntryMapper<JMeterLogEntry>> initializeMappers() {

        Map<String, LogEntryMapper<JMeterLogEntry>> mappers = new HashMap<>();

        mappers.put("timeStamp",
                (StringEntryMapper<JMeterLogEntry>) (value, variable, e) -> e.setTimestamp(Long.parseLong(value)));

        mappers.put("elapsed",
                (StringEntryMapper<JMeterLogEntry>) (value, variable, e) -> e.setDurationInMillis(Integer.parseInt(value)));

        // response code can also contain text, such as:
        // "Non HTTP response code: java.net.UnknownHostException"!
        // ==> use code 500 in these cases (to make it a failure!)
        mappers.put("responseCode",
                (StringEntryMapper<JMeterLogEntry>) (value, variable, e) -> {
                    try {
                        e.setCode(Integer.parseInt(value));
                    } catch (NumberFormatException ex) {
                        e.setCode(500);
                    }
                }
        );

        mappers.put("success",
                (StringEntryMapper<JMeterLogEntry>) (value, variable, e) -> e.setSuccess(Boolean.parseBoolean(value)));

        // label is used as URL, not all jmeter log files have URL column
        // and it maps to the values used in jmeter's own reports
        mappers.put("label",
                (StringEntryMapper<JMeterLogEntry>) (value, variable, e) -> e.setUrl(value));

        mappers.put("URL",
                (StringEntryMapper<JMeterLogEntry>) (value, variable, e) -> {
                    if (value == null || value.equals("null")) {
                        e.setLogLineType(JMeterLogLineType.TRANSACTION);
                    }
                    else {
                        e.setLogLineType(JMeterLogLineType.SAMPLE);
                    }
                });

        mappers.put("responseMessage",
                (StringEntryMapper<JMeterLogEntry>) (value, variable, e) -> {
                    // determine if SAMPLE or TRANSACTION line, fallback: only when URL field is not present!
                    if (e.getLogLineType() == null) {
                        if (value != null && value.contains("Number of samples in transaction")) {
                            e.setLogLineType(JMeterLogLineType.TRANSACTION);
                        }
                        else {
                            e.setLogLineType(JMeterLogLineType.SAMPLE);
                        }
                    }
                });

        return mappers;
    }

    /**
     * This parseLogLine takes into consideration that entries can be within double quotes.
     * No matching on literals is made within double quotes.
     * Spaces between double quoted entries and the comma's are not allowed.
     */
    public JMeterLogEntry parseLogLine(final String logline) {

        JMeterLogEntry entry = new JMeterLogEntry();

        entry.setLogline(logline);

        LogbackDirective var = null;

        boolean isBetweenQuotes = logline.startsWith(DOUBLE_QUOTE);
        int locationInLine = isBetweenQuotes ? 1 : 0;
        int skipForSearch = 0;

        for (LogbackElement element : elements) {
            if (isBetweenQuotes) { skipForSearch = logline.indexOf(DOUBLE_QUOTE + ",", locationInLine) - locationInLine; }
            if (element instanceof LogbackLiteral) {
                String search = ((LogbackLiteral) element).getLiteral();
                // if search length is zero, and it is not the first element (var == null),
                // there are two variables without separator, take all
                boolean isLastParsableEntry = search.length() == 0 && var != null;
                int idx =  isLastParsableEntry ? logline.length() : logline.indexOf(search, locationInLine + skipForSearch);
                if (var != null) {
                    String directive = var.getDirective();
                    log.trace("lookup: {} var: {}", directive, var);
                    String value;
                    try {
                        value = logline.substring(locationInLine, idx - (isBetweenQuotes ? 1 : 0));
                    } catch (StringIndexOutOfBoundsException e) {
                        throw new LogRaterException("Problem parsing log line searching '" + search + "' for " + var + " in logline " + logline, e);
                    }

                    entry.addField(directive, value);

                    { // reset the "in between quotes" values after adding value
                        isBetweenQuotes = false;
                        skipForSearch = 0;
                    }

                    LogEntryMapper<JMeterLogEntry> mapper = mappers.get(directive);
                    if (mapper != null) {
                        mapper.writeToLogEntry(value, var.getVariable(), entry);
                    }
                }
                locationInLine = idx + search.length();
                if (locationInLine < logline.length() && logline.charAt(locationInLine) == DOUBLE_QUOTE_CHAR) { locationInLine++; isBetweenQuotes = true; }
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
        // example line: timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect
        // note that some fields can start and end with double quotes and can contain comma's themselves,
        // such as responseMessage and failureMessage, but not always!!
        // e.g. responseMessage can be: ,OK, or ,"successes 1, failures 0",
        List<LogbackElement> elements = new ArrayList<>();

        pattern = pattern.trim();

        StringBuilder literal = new StringBuilder();
        char[] jmeterPattern = pattern.toCharArray();

        for (char c : jmeterPattern) {
            if (c == ',') {
                LogbackDirective entry = LogbackDirective.from(literal.toString());
                elements.add(entry);
                literal.setLength(0);
                elements.add(new LogbackLiteral(","));
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

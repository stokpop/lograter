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
package nl.stokpop.lograter.processor.accesslog;

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.SessionIdParser;
import nl.stokpop.lograter.util.linemapper.LineMap;
import nl.stokpop.lograter.util.linemapper.LineMapperCallback;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

@NotThreadSafe
public class AccessLogToCsvProcessor implements Processor<AccessLogEntry> {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS").withLocale(Locale.US);
    private static final char SEPARATOR = ';';
    private static final LineMap NO_MAPPER = new LineMap("", "NO-MAPPER");
    private static final String HEADER_FIELDS = "start-time;end-time;duration-ms;session-id;url;mapper-name;mapper-regexp;user-agent";

    private final PrintWriter csvWriter;
    private final LineMapperSection mapper;

    private final SessionIdParser sessionIdParser;

    private boolean firstLine = true;

	public AccessLogToCsvProcessor(OutputStream csvOutputStream, LineMapperSection mapper, SessionIdParser sessionIdParser) {
        this.csvWriter = FileUtils.createBufferedPrintWriterWithUTF8(csvOutputStream);
        this.mapper = mapper;
        this.sessionIdParser = sessionIdParser;
    }

    @Override
	public void processEntry(final AccessLogEntry entry) {

		// only use the url lines
        final String url = entry.getUrl();
        mapper.updateMappers(url, false, new LineMapperCallback() {
            @Override
            public void matchFound(LineMap mapper) {
                if (firstLine) {
                    printHeader();
                    firstLine = false;
                }
                csvWriter.println(createLine(mapper, entry));
            }

            @Override
            public void noMatchFound(String line) {
                if (firstLine) {
                    printHeader();
                    firstLine = false;
                }
                csvWriter.println(createLine(NO_MAPPER, entry));
            }

            @Override
            public void multiMatchFound(String line, int hits) {
                throw new LogRaterException("Multi match not expected in access log to csv processor. Multiple matches for url: " + line);
            }
        });
	}

    private void printHeader() {
        csvWriter.println(HEADER_FIELDS);
    }

    private String createLine(LineMap lineMap, AccessLogEntry entry) {
        // access log timestamp is the time the request started,
        // so endtime is start timestamp plus duration
        long startTimeMs = entry.getTimestamp();
        String url = entry.getUrl();
        String sessionID = sessionIdParser.parseSessionId(entry);

	    int durationInMillis = entry.getDurationInMillis();
        long endTimeMs = startTimeMs + durationInMillis;
        String startTime = dateTimeFormatter.print(startTimeMs);
        String endTime = dateTimeFormatter.print(endTimeMs);

        StringBuilder builder = new StringBuilder();

        builder.append(startTime);
        builder.append(SEPARATOR);
        builder.append(endTime);
        builder.append(SEPARATOR);
        builder.append(durationInMillis);
        builder.append(SEPARATOR);
        builder.append(sessionID);
        builder.append(SEPARATOR);
        builder.append(url);
        builder.append(SEPARATOR);
        builder.append(lineMap.getNameWithReplacementsFromLine(url).replace(SEPARATOR, '_'));
        builder.append(SEPARATOR);
        builder.append(lineMap.getRegExpPattern().replace(SEPARATOR, '_'));
        builder.append(SEPARATOR);
        builder.append(entry.getUserAgent());

        return builder.toString();

    }

}

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
package nl.stokpop.lograter.sar;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.sar.entry.SarCpuEntry;
import nl.stokpop.lograter.sar.entry.SarParserException;
import nl.stokpop.lograter.sar.entry.SarSwapEntry;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses SAR files.
 */
public class SarParser {

    private static final Logger log = LoggerFactory.getLogger(SarParser.class);
    // Linux 2.6.18-398.el5 (myserver.domain.nl) 	2014-11-28
    private static final Pattern sarHeaderEl5Pattern = Pattern
            .compile("Linux (.*)\\s*\\((.*)\\)\\s*(\\S*)");

    // Linux 2.6.32-504.el6.x86_64 (myserver.domain.nl) 	2015-01-29 	_x86_64_	(2 CPU)
    private static final Pattern sarHeaderPattern = Pattern
            .compile("Linux (.*)\\s*\\((.*)\\)\\s*(\\S*)\\s*(\\S*)\\s*\\((\\d*) CPU\\)");

    // RHEL7
    // Linux 3.10.0-327.10.1.el7.x86_64 (myserver.domain.nl) 	2016-05-11 	_x86_64_	(2 CPU)

    // 00:00:01        CPU      %usr     %nice      %sys   %iowait    %steal      %irq     %soft    %guest     %idle
    // 00:05:02        all     45.40      0.01      7.96      7.79      0.00      0.39      2.12      0.00     36.33
    // 00:05:02          0     50.17      0.01      7.62      6.82      0.00      0.79      3.95      0.00     30.63
    // 00:05:02          1     40.68      0.02      8.29      8.74      0.00      0.00      0.31      0.00     41.96
    private static final Pattern cpuPattern = Pattern
            .compile("(\\d\\d:\\d\\d:\\d\\d(?:\\s\\S{2})?)\\s*(\\S*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)");

    // 00:00:01          CPU     %user     %nice   %system   %iowait    %steal     %idle
    // 00:05:01          all      3.14      0.01      1.03      5.12      0.00     90.71
    // 00:05:01            0      3.24      0.00      1.01      4.90      0.00     90.85
    // 00:05:01            1      3.04      0.00      1.06      5.33      0.00     90.57
    private static final Pattern cpuShortPattern = Pattern
            .compile("(\\d\\d:\\d\\d:\\d\\d(?:\\s\\S{2})?)\\s*(\\S*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)$");

    // RHEL7 format adds %gnice column
    // 00:00:01        CPU      %usr     %nice      %sys   %iowait    %steal      %irq     %soft    %guest    %gnice     %idle
    // 00:05:01        all      3.43      0.10      1.27      0.29      0.00      0.00      0.02      0.00      0.00     94.89
    // 00:05:01          0      2.07      0.15      1.00      0.22      0.00      0.00      0.02      0.00      0.00     96.53
    // 00:05:01          1      4.80      0.04      1.54      0.35      0.00      0.00      0.01      0.00      0.00     93.25
    private static final Pattern cpuPatternWithGnice = Pattern
            .compile("(\\d\\d:\\d\\d:\\d\\d(?:\\s\\S{2})?)\\s*(\\S*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)");

	// mpstat command adds interrupts column at the end
	// 00:00:01        CPU      %usr     %nice      %sys   %iowait    %steal      %irq     %soft    %guest    %gnice     %idle   %interrupts
	// 00:05:01        all      3.43      0.10      1.27      0.29      0.00      0.00      0.02      0.00      0.00     94.89   12491
	// 00:05:01          0      2.07      0.15      1.00      0.22      0.00      0.00      0.02      0.00      0.00     96.53   11848
	// 00:05:01          1      4.80      0.04      1.54      0.35      0.00      0.00      0.01      0.00      0.00     93.25   10837
	private static final Pattern cpuPatternWithGniceAndInterrupts = Pattern
			.compile("(\\d\\d:\\d\\d:\\d\\d(?:\\s\\S{2})?)\\s*(\\S*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)\\s*([\\d.]*)");

	// 00:00:01     pswpin/s pswpout/s
    // 00:05:01         0.02      0.00
    // 00:10:01         0.00      0.00
    private static final Pattern swpPattern = Pattern
            .compile("(\\d\\d:\\d\\d:\\d\\d(?:\\s\\S{2})?)\\s*([\\d.]*)\\s*([\\d.]*)");

    private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd").withLocale(new Locale("US"));
    private static final DateTimeFormatter dateFormatterPosix = DateTimeFormat.forPattern("MM/dd/yy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss").withLocale(new Locale("US"));
    private static final DateTimeFormatter timeFormatterAMPM = DateTimeFormat.forPattern("hh:mm:ss a").withLocale(new Locale("US"));

    public SarLog parseSarLog(File[] sarFiles, TimePeriod timePeriod) throws IOException {
        SarLog overallSarLog = new SarLog();
        for (File sarFile : sarFiles) {
            SarLog sarLog = parseSarLog(sarFile, timePeriod);
            overallSarLog.add(sarLog);
        }
        return overallSarLog;
    }

    public SarLog parseSarLog(Reader sarReader, TimePeriod timePeriod) throws IOException {

        BufferedReader bufferedReader = FileUtils.createBufferedReader(sarReader);

        String line;
        SarLog sarLog = new SarLog();
        ParserState state = ParserState.INITIAL;
        ParseContext parseContext = new ParseContext();
        parseContext.setTimePeriod(timePeriod);
        while ((line = bufferedReader.readLine()) != null) {
            parseContext.setCurrentLine(line);
            state = state.nextState(parseContext);
            switch (state) {
                case INITIAL:
                    // nothing to do
                    break;
                case HEADER:
                    parseHeader(sarLog, parseContext);
                    break;
                case CPU_HEADER:
                    break;
                case CPU_LINE:
                    parseCpuLine(sarLog, parseContext);
                    break;
                case SWP_HEADER:
                    break;
                case SWP_LINE:
                    parseSwapLine(sarLog, parseContext);
                    break;
                case OTHER:
                    break;
                case INBETWEEN:
                    break;
                case END:
                    break;
                default:
                    throw new IllegalStateException(String.format("Illegal parser state '%s", state));
            }
        }

        return sarLog;

    }

    public SarLog parseSarLog(File sarFile, TimePeriod timePeriod) throws IOException {

        if (!sarFile.exists()) {
            throw new LogRaterException("No file found: " + sarFile.getAbsolutePath());
        }

        log.info("Start sar log file parsing: {}", sarFile.getAbsolutePath());

        BufferedReader sarFileReader = FileUtils.createBufferedReader(sarFile);

        return parseSarLog(sarFileReader, timePeriod);

    }

    private void parseSwapLine(SarLog sarLog, ParseContext parseContext) {
        String line = parseContext.getCurrentLine();
        long fileDate = parseContext.getFileDate();

        Matcher sarSwpLineMatcher = swpPattern.matcher(line);

        if (sarSwpLineMatcher.find()) {
            String timeString = sarSwpLineMatcher.group(1);
            LocalTime time = parseLocalTime(timeString);
            DateTime mergedDateTime = new LocalDate(fileDate).toDateTime(time);
            double pSwpInPerSec = Double.parseDouble(sarSwpLineMatcher.group(2));
            double pSwpOutPerSec = Double.parseDouble(sarSwpLineMatcher.group(3));
            SarSwapEntry entry = new SarSwapEntry(mergedDateTime.getMillis(), pSwpInPerSec, pSwpOutPerSec);
            if (parseContext.isWithinTimePeriod(entry.getTimestamp())) {
                sarLog.addSwapEntry(entry);
            }
        }

    }

    private void parseHeader(SarLog sarLog, ParseContext parseContext) {
        String line = parseContext.getCurrentLine();

        Matcher sarHeaderEl5Matcher = sarHeaderEl5Pattern.matcher(line);
        Matcher sarHeaderMatcher = sarHeaderPattern.matcher(line);

        String dateString;
        int cpuCount;
        if (sarHeaderMatcher.find()) {
            dateString = sarHeaderMatcher.group(3);
            cpuCount = Integer.parseInt(sarHeaderMatcher.group(5));
        }
        else if (sarHeaderEl5Matcher.find()) {
            dateString = sarHeaderEl5Matcher.group(3);
            cpuCount = -1;
        }
        else {
            String error = String.format("Unable to parse header line: [%s]", line);
            throw new LogRaterException(error);
        }
        LocalDate dateTime = dateString.contains("/") ? dateFormatterPosix.parseLocalDate(dateString) : dateFormatter.parseLocalDate(dateString);
        parseContext.setFileDate(dateTime.toDate().getTime());
        sarLog.setCpuCount(cpuCount);
    }

    private void parseCpuLine(SarLog sarLog, ParseContext parseContext) {
        String line = parseContext.getCurrentLine();

        if (line.startsWith("Average")) {
            log.debug("Skipping average cpu line [{}]", line);
            return;
        }

        long fileDateTimestamp = parseContext.getFileDate();
        LocalDate fileDate = new LocalDate(fileDateTimestamp);

        SarCpuEntry cpuAllEntry;

        try {
            cpuAllEntry = createSarCpuEntry(line, fileDate);
        } catch (SarParserException e) {
            log.warn("Skipping line that cannot be parsed [" + line + "] due to [" + e.getMessage() + "]", e);
            return;
        }

        if (cpuAllEntry.getCPU().equals("all")) {
            if (parseContext.isWithinTimePeriod(cpuAllEntry.getTimestamp())) {
                sarLog.addCpuAllEntry(cpuAllEntry);
            }
        }
        else {
            // the first CPU is 0
            int cpuCount = Integer.parseInt(cpuAllEntry.getCPU()) + 1;
            if (cpuCount > sarLog.getCpuCount()) {
                sarLog.setCpuCount(cpuCount);
            }
            log.debug("Skipping non-all CPU line: [{}]", line);
        }
    }

    private SarCpuEntry createSarCpuEntry(String line, LocalDate fileDate) throws NumberFormatException {
        // the long match also matches short lines, causing not all groups to be filled.
        // so count count to determine a long or short cpu line
        // NOTE: am or pm adds another token, so count can be one higher
        StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
        int count = stringTokenizer.countTokens();

        Matcher cpuMatcher;
        SarCpuEntry cpuAllEntry;
        boolean lineContainsAMPM = line.toLowerCase().contains(" am ") || line.toLowerCase().contains(" pm ");
        if (((count == 13) || (count == 12 && !lineContainsAMPM)) && (cpuMatcher = cpuPatternWithGnice.matcher(line)).find()) {
            cpuAllEntry = createSarGniceCpuEntry(cpuMatcher, fileDate);
        }
        else if ((count == 11 || count == 12) && (cpuMatcher = cpuPattern.matcher(line)).find()) {
            cpuAllEntry = createSarLongCpuEntry(cpuMatcher, fileDate);
        }
        else if ((count == 8 || count == 9) && (cpuMatcher = cpuShortPattern.matcher(line)).find()) {
            cpuAllEntry = createShortSarCpuEntry(cpuMatcher, fileDate);
        }
        else {
            String error = String.format("Unable to parse cpu line, unexpected format: [%s]", line);
            throw new SarParserException(error);
        }
        return cpuAllEntry;
    }

    private SarCpuEntry createShortSarCpuEntry(Matcher cpuShortMatcher, LocalDate fileDate) throws SarParserException {
            String timeString = cpuShortMatcher.group(1);
            LocalTime time = parseLocalTime(timeString);
            DateTime mergedDateTime = fileDate.toDateTime(time);
        try {
            String CPU = cpuShortMatcher.group(2);
            double user = Double.parseDouble(cpuShortMatcher.group(3));
            double nice = Double.parseDouble(cpuShortMatcher.group(4));
            double system = Double.parseDouble(cpuShortMatcher.group(5));
            double iowait = Double.parseDouble(cpuShortMatcher.group(6));
            double steal = Double.parseDouble(cpuShortMatcher.group(7));
            double idle = Double.parseDouble(cpuShortMatcher.group(8));

            return new SarCpuEntry(mergedDateTime.getMillis(), CPU, user, nice, system, iowait, steal, idle);
        } catch (NumberFormatException e) {
            throw new SarParserException("Unable to parse number.", e);
        }
    }

    private SarCpuEntry createSarGniceCpuEntry(Matcher cpuMatcher, LocalDate fileDate) throws SarParserException {
        String timeString = cpuMatcher.group(1);
        LocalTime time = parseLocalTime(timeString);
        DateTime mergedDateTime = fileDate.toDateTime(time);
        String CPU = cpuMatcher.group(2);

        try {
            double usr = Double.parseDouble(cpuMatcher.group(3));
            double nice = Double.parseDouble(cpuMatcher.group(4));
            double sys = Double.parseDouble(cpuMatcher.group(5));
            double iowait = Double.parseDouble(cpuMatcher.group(6));
            double steal = Double.parseDouble(cpuMatcher.group(7));
            double irq = Double.parseDouble(cpuMatcher.group(8));
            double soft = Double.parseDouble(cpuMatcher.group(9));
            double guest = Double.parseDouble(cpuMatcher.group(10));
            double gnice = Double.parseDouble(cpuMatcher.group(11));
            double idle = Double.parseDouble(cpuMatcher.group(12));

            return new SarCpuEntry(mergedDateTime.getMillis(), CPU, usr, nice, sys, iowait, steal, irq, soft, guest, gnice, idle);
        } catch (NumberFormatException e) {
            throw new SarParserException("Unable to parse number.", e);
        }
    }

    private SarCpuEntry createSarLongCpuEntry(Matcher cpuMatcher, LocalDate fileDate) throws SarParserException {
        String timeString = cpuMatcher.group(1);
        LocalTime time = parseLocalTime(timeString);
        DateTime mergedDateTime = fileDate.toDateTime(time);
        String CPU = cpuMatcher.group(2);

        try {
            double usr = Double.parseDouble(cpuMatcher.group(3));
            double nice = Double.parseDouble(cpuMatcher.group(4));
            double sys = Double.parseDouble(cpuMatcher.group(5));
            double iowait = Double.parseDouble(cpuMatcher.group(6));
            double steal = Double.parseDouble(cpuMatcher.group(7));
            double irq = Double.parseDouble(cpuMatcher.group(8));
            double soft = Double.parseDouble(cpuMatcher.group(9));
            double guest = Double.parseDouble(cpuMatcher.group(10));
            double idle = Double.parseDouble(cpuMatcher.group(11));

            // 0 for gnice which is not in this sar file format
            return new SarCpuEntry(mergedDateTime.getMillis(), CPU, usr, nice, sys, iowait, steal, irq, soft, guest, 0, idle);
        } catch (NumberFormatException e) {
            throw new SarParserException("Unable to parse number.", e);
        }
    }

    private static LocalTime parseLocalTime(String timeString) {
        String lowerCase = timeString.toLowerCase();
        if (lowerCase.contains("am") || lowerCase.contains("pm")) {
            return timeFormatterAMPM.parseLocalTime(timeString);
        }
        else {
            return timeFormatter.parseLocalTime(timeString);
        }
    }

}

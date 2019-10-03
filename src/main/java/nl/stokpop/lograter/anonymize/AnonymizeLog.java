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
package nl.stokpop.lograter.anonymize;

import nl.stokpop.lograter.feeder.FileFeeder;
import nl.stokpop.lograter.logentry.LogbackLogEntry;
import nl.stokpop.lograter.parser.ApplicationLogParser;
import nl.stokpop.lograter.parser.IisLogParser;
import nl.stokpop.lograter.parser.line.IisLogFormatParser;
import nl.stokpop.lograter.parser.line.LogbackParser;
import nl.stokpop.lograter.util.InLineReplacer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static nl.stokpop.lograter.util.InLineReplacer.THREE_LETTER_HASH;
import static nl.stokpop.lograter.util.InLineReplacer.replaceBlacklistedWords;
import static nl.stokpop.lograter.util.InLineReplacer.replaceDomains;
import static nl.stokpop.lograter.util.InLineReplacer.replaceIPv4;
import static nl.stokpop.lograter.util.InLineReplacer.replaceSeparatedWords;
import static nl.stokpop.lograter.util.InLineReplacer.replaceWordsMapperLine;

public class AnonymizeLog {

    public enum LogType { accesslog, mapperfile, cachelog, logback, iis }

    /**
     * Provide a log file to anonymize and possibly a list of blacklisted words.
     * Blacklisted words will be replaced by a three letter hash.
     */
    public static void main(String[] args) {

        String type = args[0];
        
        String filename = args[1];
        File file = new File(filename);

        PrintStream printStream = System.out;

        if (LogType.accesslog.name().equalsIgnoreCase(type)) {
            List<String> blacklist = args.length > 2 ? Arrays.asList(args[2].split("#")) : Collections.emptyList();
            new AnonymizeLog().anonymizeAccessLogFile(file, blacklist, printStream);
        }
        else if (LogType.mapperfile.name().equalsIgnoreCase(type)) {
            List<String> blacklist = args.length > 2 ? Arrays.asList(args[2].split("#")) : Collections.emptyList();
            new AnonymizeLog().anonymizeMapperFile(file, blacklist, printStream);
        }
        else if (LogType.cachelog.name().equalsIgnoreCase(type)) {
            new AnonymizeLog().anonymizeCacheLog(file, printStream);
        }
        else if (LogType.logback.name().equalsIgnoreCase(type)) {
            String logpattern = args.length > 2 ? args[2] : "";
            new AnonymizeLog().anonymizeLogback(file, logpattern, printStream);
        }
        else if (LogType.iis.name().equalsIgnoreCase(type)) {
            String logpattern = args.length > 2 ? args[2] : "";
            new AnonymizeLog().anonymizeIisLog(file, logpattern, printStream);
        }
    }

    private void anonymizeIisLog(File file, String logpattern, PrintStream printStream) {
        IisLogFormatParser iisLogFormatParser = IisLogFormatParser.createIisLogFormatParser(logpattern);

        IisLogAnonymizeProcessor processor = new IisLogAnonymizeProcessor(printStream);
        
        IisLogParser iisLogParser = new IisLogParser(iisLogFormatParser);
        iisLogParser.addProcessor(processor);

        FileFeeder feeder = new FileFeeder();
        feeder.feedFilesAsString(Collections.singletonList(file.getAbsolutePath()), iisLogParser);
    }

    private void anonymizeLogback(File file, String logbackPattern, PrintStream printStream) {
        LogbackParser<LogbackLogEntry> logbackParser = LogbackParser.createLogbackParser(logbackPattern);

        LogbackLogAnonymizeProcessor processor = new LogbackLogAnonymizeProcessor(printStream);

        ApplicationLogParser appLogParser = new ApplicationLogParser(logbackParser);
        appLogParser.addProcessor(processor);

        FileFeeder feeder = new FileFeeder();
        feeder.feedFilesAsString(Collections.singletonList(file.getAbsolutePath()), appLogParser);
    }

    private void anonymizeCacheLog(File file, PrintStream printStream) {
        List cacheWhiteList = Arrays.asList("MISS", "HIT", "READ", "STORE", "INVALIDATE");
        try (Stream<String> stream = Files.lines(Paths.get(file.toURI()))) {
            stream
                    .map(line -> replaceSeparatedWords(line, ";", cacheWhiteList, THREE_LETTER_HASH))
                    .forEach(printStream::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void anonymizeMapperFile(File file, List<String> blacklist, PrintStream printStream) {
        try (Stream<String> stream = Files.lines(Paths.get(file.toURI()))) {
            stream
                    .map(line -> line.contains("###") ? replaceWordsMapperLine(line, THREE_LETTER_HASH) : line )
                    .map(line -> replaceBlacklistedWords(line, blacklist, THREE_LETTER_HASH))
                    .forEach(printStream::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void anonymizeAccessLogFile(File file, List<String> blacklist, PrintStream printStream) {
        try (Stream<String> stream = Files.lines(Paths.get(file.toURI()))) {
            stream
                    .map(line -> replaceIPv4(line,"1.2.3.$1"))
                    .map(line -> replaceDomains(line, "stokpop.nl", THREE_LETTER_HASH))
                    .map(s -> InLineReplacer.replaceUrlInRequestTriplet(s, THREE_LETTER_HASH))
                    .map(s -> InLineReplacer.replaceDomainWithUrl(s, THREE_LETTER_HASH))
                    .map(s -> InLineReplacer.replaceEncryptedValue(s, THREE_LETTER_HASH))
                    .map(line -> replaceBlacklistedWords(line, blacklist, THREE_LETTER_HASH))
                    .forEach(printStream::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

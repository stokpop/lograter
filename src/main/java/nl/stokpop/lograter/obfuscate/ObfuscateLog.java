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
package nl.stokpop.lograter.obfuscate;

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

import static nl.stokpop.lograter.util.InLineReplacer.*;

public class ObfuscateLog {

    public enum LogType { accesslog, mapperfile, cachelog, logback, iis, any }

    /**
     * Provide a log file to obfuscate and possibly a list of to-be-obfuscated words.
     * Obfuscated words are replaced by a three letter hash/word.
     */
    public static void main(String[] args) {

        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: java -jar ObfuscateLog <logtype> <logfile> [list of # separated words]\n" +
                "logtypes: accesslog, mapperfile, cachelog, logback, iis, any\n" +
                "logfile: path to logfile\n" +
                "words (optional): list of # separated words to be obfuscated");
            System.exit(1);
        }

        String type = args[0];
        
        String filename = args[1];
        File file = new File(filename);

        PrintStream printStream = System.out;

        if (LogType.accesslog.name().equalsIgnoreCase(type)) {
            List<String> obfuscateWords = args.length > 2 ? Arrays.asList(args[2].split("#")) : Collections.emptyList();
            new ObfuscateLog().obfuscateAccessLogFile(file, obfuscateWords, printStream);
        }
        else if (LogType.mapperfile.name().equalsIgnoreCase(type)) {
            List<String> obfuscateWords = args.length > 2 ? Arrays.asList(args[2].split("#")) : Collections.emptyList();
            new ObfuscateLog().obfuscateMapperFile(file, obfuscateWords, printStream);
        }
        else if (LogType.cachelog.name().equalsIgnoreCase(type)) {
            new ObfuscateLog().obfuscateCacheLog(file, printStream);
        }
        else if (LogType.logback.name().equalsIgnoreCase(type)) {
            String logpattern = args.length > 2 ? args[2] : "";
            new ObfuscateLog().obfuscateLogback(file, logpattern, printStream);
        }
        else if (LogType.iis.name().equalsIgnoreCase(type)) {
            String logpattern = args.length > 2 ? args[2] : "";
            new ObfuscateLog().obfuscateIisLog(file, logpattern, printStream);
        }
        else if (LogType.any.name().equalsIgnoreCase(type)) {
            List<String> obfuscateWords = args.length > 2 ? Arrays.asList(args[2].split("#")) : Collections.emptyList();
            new ObfuscateLog().obfuscateAnyLog(file, obfuscateWords, printStream);
        }
    }

    private void obfuscateIisLog(File file, String logpattern, PrintStream printStream) {
        IisLogFormatParser iisLogFormatParser = IisLogFormatParser.createIisLogFormatParser(logpattern);

        IisLogObfuscateProcessor processor = new IisLogObfuscateProcessor(printStream);
        
        IisLogParser iisLogParser = new IisLogParser(iisLogFormatParser);
        iisLogParser.addProcessor(processor);

        FileFeeder feeder = new FileFeeder(Collections.singletonList(file));
        feeder.feed(iisLogParser);
    }

    private void obfuscateLogback(File file, String logbackPattern, PrintStream printStream) {
        LogbackParser<LogbackLogEntry> logbackParser = LogbackParser.createLogbackParser(logbackPattern);

        LogbackLogObfuscateProcessor processor = new LogbackLogObfuscateProcessor(printStream);

        ApplicationLogParser appLogParser = new ApplicationLogParser(logbackParser);
        appLogParser.addProcessor(processor);

        FileFeeder feeder = new FileFeeder(Collections.singletonList(file));
        feeder.feed(appLogParser);
    }

    private void obfuscateCacheLog(File file, PrintStream printStream) {
        List<String> cacheWhiteList = Arrays.asList("MISS", "HIT", "READ", "STORE", "INVALIDATE");
        try (Stream<String> stream = Files.lines(Paths.get(file.toURI()))) {
            stream
                    .map(line -> replaceSeparatedWords(line, ";", cacheWhiteList, THREE_LETTER_HASH))
                    .forEach(printStream::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void obfuscateMapperFile(File file, List<String> obfuscateWords, PrintStream printStream) {
        try (Stream<String> stream = Files.lines(Paths.get(file.toURI()))) {
            stream
                    .map(line -> line.contains("###") ? replaceWordsMapperLine(line, THREE_LETTER_HASH) : line )
                    .map(line -> replaceObfuscateWords(line, obfuscateWords, THREE_LETTER_HASH))
                    .forEach(printStream::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void obfuscateAccessLogFile(File file, List<String> obfuscateWords, PrintStream printStream) {
        try (Stream<String> stream = Files.lines(Paths.get(file.toURI()))) {
            stream
                    .map(line -> replaceIPv4(line,"1.2.3.$1"))
                    .map(line -> replaceDomains(line, "example.org", THREE_LETTER_HASH))
                    .map(s -> InLineReplacer.replaceUrlInRequestTriplet(s, THREE_LETTER_HASH))
                    .map(s -> InLineReplacer.replaceDomainWithUrl(s, THREE_LETTER_HASH))
                    .map(s -> InLineReplacer.replaceEncryptedValue(s, THREE_LETTER_HASH))
                    .map(line -> replaceObfuscateWords(line, obfuscateWords, THREE_LETTER_HASH))
                    .forEach(printStream::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void obfuscateAnyLog(File file, List<String> obfuscateWords, PrintStream printStream) {
        try (Stream<String> stream = Files.lines(Paths.get(file.toURI()))) {
            stream
                    .map(line -> replaceIPv4(line,"1.2.3.$1"))
                    .map(line -> replaceDomains(line, "example.org", THREE_LETTER_HASH))
                    .map(s -> InLineReplacer.replaceUrlInRequestTriplet(s, THREE_LETTER_HASH))
                    .map(s -> InLineReplacer.replaceDomainWithUrl(s, THREE_LETTER_HASH))
                    .map(s -> InLineReplacer.replaceEncryptedValue(s, THREE_LETTER_HASH))
                    .map(line -> replaceObfuscateWords(line, obfuscateWords, THREE_LETTER_HASH))
                    .forEach(printStream::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

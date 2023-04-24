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
package nl.stokpop.lograter.obfuscate;

import nl.stokpop.lograter.logentry.LogbackLogEntry;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.InLineReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogbackLogObfuscateProcessor implements Processor<LogbackLogEntry> {

	private static final Logger log = LoggerFactory.getLogger(LogbackLogObfuscateProcessor.class);

	private final PrintStream printStream;

	public LogbackLogObfuscateProcessor(PrintStream printStream) {
	    this.printStream = printStream;
    }

	@Override
	public void processEntry(LogbackLogEntry entry) {
        String logger = entry.getLogger();
        String classname = entry.getClassName();
        String message = entry.getMessage();
        String newLogLine = entry.getLogline();
        String threadName = entry.getThreadName();
        if (logger != null) newLogLine = newLogLine.replaceAll(Pattern.quote(logger), InLineReplacer.replaceClassname(logger, InLineReplacer.THREE_LETTER_HASH));
        if (classname != null) newLogLine = newLogLine.replaceAll(Pattern.quote(classname), InLineReplacer.replaceClassname(classname, InLineReplacer.THREE_LETTER_HASH));
        if (message != null) newLogLine = newLogLine.replaceAll(Pattern.quote(message), InLineReplacer.replaceWords(message, InLineReplacer.THREE_LETTER_HASH));
        if (threadName != null) newLogLine = newLogLine.replaceAll(Pattern.quote(threadName), "Thread-" + InLineReplacer.replaceWords(threadName, InLineReplacer.THREE_LETTER_HASH));
        for (String field : entry.getCustomFields()) {
            String value = entry.getField(field);
            if (value != null ) newLogLine = newLogLine.replaceAll(Pattern.quote(value), InLineReplacer.replaceWords(value, InLineReplacer.THREE_LETTER_HASH));
        }
        printStream.println(newLogLine);
        String nonLogLines = Arrays.stream(entry.getNonLogLines())
                .map(s -> InLineReplacer.replaceWords(s, InLineReplacer.THREE_LETTER_HASH))
                .collect(Collectors.joining("\n"));
        if (nonLogLines.length() > 0) {
            printStream.println(nonLogLines);
        }
	}
}

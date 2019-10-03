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

import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.InLineReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.regex.Pattern;

public class IisLogAnonymizeProcessor implements Processor<AccessLogEntry> {

	private static final Logger log = LoggerFactory.getLogger(IisLogAnonymizeProcessor.class);

	private final PrintStream printStream;

	public IisLogAnonymizeProcessor(PrintStream printStream) {
	    this.printStream = printStream;
    }

	@Override
	public void processEntry(AccessLogEntry entry) {
        String url = entry.getUrl();
        String referrer = entry.getReferrer();
        String urlQuery = entry.getField("cs-uri-query");
        String ip = entry.getField("c-ip");
        String cookie = entry.getField("cs(Cookie)");
        String userAgent = entry.getUserAgent();

        String newLogLine = entry.getLogline();
        if (url != null) newLogLine = newLogLine.replaceAll(Pattern.quote(url), InLineReplacer.replaceUrlPathAfterSlashes(url, InLineReplacer.THREE_LETTER_HASH));
        if (referrer != null) newLogLine = newLogLine.replaceAll(Pattern.quote(referrer), InLineReplacer.replaceUrlPathAfterSlashes(referrer, InLineReplacer.THREE_LETTER_HASH));
        if (urlQuery != null) newLogLine = newLogLine.replaceAll(Pattern.quote(urlQuery), InLineReplacer.replaceWords(urlQuery, InLineReplacer.THREE_LETTER_HASH));
        if (ip != null) newLogLine = newLogLine.replaceAll(Pattern.quote(ip), InLineReplacer.replaceIPv4(ip, "1.2.3.$1"));
        if (cookie != null) newLogLine = newLogLine.replaceAll(Pattern.quote(cookie), InLineReplacer.replaceWords(cookie, InLineReplacer.THREE_LETTER_HASH));
        if (cookie != null) newLogLine = newLogLine.replaceAll(Pattern.quote(userAgent), InLineReplacer.replaceWords(userAgent, InLineReplacer.THREE_LETTER_HASH));
        printStream.println(newLogLine);
	}
}

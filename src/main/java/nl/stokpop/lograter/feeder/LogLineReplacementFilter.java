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
package nl.stokpop.lograter.feeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter for feeders to replace parts of incoming log lines.
 */
public class LogLineReplacementFilter extends ChainedFeeder {

    private static final Logger log = LoggerFactory.getLogger(LogLineReplacementFilter.class);

    private final Pattern regexp;
    private final String replacement;


    public LogLineReplacementFilter(String name, String regexp, String replacement) {
        super(name);
        this.regexp = Pattern.compile(regexp);
        this.replacement = replacement;
    }

    @Override
    public String executeFeeder(String filename, String logLine) {
        Matcher matcher = regexp.matcher(logLine);
        if (matcher.find()) {
            String newLogLine = matcher.replaceAll(replacement);
            log.trace("Replaced [{}] with [{}]", logLine, newLogLine);
            return newLogLine;
        }
        return logLine;
    }
}

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
package nl.stokpop.lograter.util.linemapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineMap {

	private Logger log = LoggerFactory.getLogger(LineMap.class);

	private static final char[] REGEXP_CHARS = {'[', '(', '{', '.', '*', '+', '?', '|', '^', '$'};

	private final Pattern pattern;
	private final String regExpPattern;
	private final String regExpPrefix;
	private final String name;

	public LineMap(String regExpPattern, String name) {
		this.regExpPattern = regExpPattern;
		this.regExpPrefix = extractPrefixToMatch(regExpPattern);
		this.pattern = Pattern.compile(regExpPattern);
        this.name = sanityCheckReplacementGroups(regExpPattern, name, pattern);
    }

	private String extractPrefixToMatch(String regExpPattern) {
		if (regExpPattern.startsWith("^")) {
			regExpPattern = regExpPattern.substring(1);
		}
		int index = -1;
		for (int i = 0; i < regExpPattern.length(); i++) {
			char c = regExpPattern.charAt(i);
			if (c == '\\') {
				// Skip the next character if it is escaped by a backslash
				i++;
			} else {
				for (char regExpChar : REGEXP_CHARS) {
					if (c == regExpChar) {
						index = i;
						break;
					}
				}
			}
			if (index != -1) {
				break;
			}
		}
		String prefixToReturn = index == -1 ? regExpPattern : regExpPattern.substring(0, index);
		// remove backslashes from the prefix
		return prefixToReturn.replace("\\", "");
	}

	private String sanityCheckReplacementGroups(String regExpPattern, String name, Pattern pattern) {
        Matcher matcher = pattern.matcher(regExpPattern);
        try {
			String ignore = matcher.replaceFirst(name);
		} catch (IndexOutOfBoundsException e) {
            String message = String.format("Found replacements in [%s], but no matching groups in [%s]. Continue without group replacement activated!", name, regExpPattern);
            log.warn(message);
            return Matcher.quoteReplacement(name);
        }
        return name;
    }

    public String getName() {
		return name;
	}
	
	boolean isMatch(String line) {

		// if does not seem like a match, return fast
		if (!line.startsWith(regExpPrefix)) {
			return false;
		}

		// check with full regexp match
		boolean match = pattern.matcher(line).matches();
		if (match) {
			log.debug("Match for mapper: [{}] on line: [{}]", name, line);
		}
		return match;
	}

	public String getNameWithReplacementsFromLine(String line) {
		Matcher matcher = pattern.matcher(line);
		return matcher.replaceFirst(name);
	}

	@Override
	public String toString() {
		return "LineMap{" +
				"pattern=" + pattern +
				", name='" + name + '\'' +
				'}';
	}

	public String getRegExpPattern() {
		return regExpPattern;
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException();
	}
}

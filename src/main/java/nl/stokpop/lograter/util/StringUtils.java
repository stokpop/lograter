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
package nl.stokpop.lograter.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {

	public StringUtils() {
		super();
	}
	
	public static int countOccurrences(String text, char charToFind) {
        if (text == null) { return 0; }

	    int count = 0;
	    for (int i = 0; i < text.length(); i++)
	    {
	        if (text.charAt(i) == charToFind) { count++; }
	    }
	    return count;
	}

	public static int countOccurrences(String text, String stringToFind) {
		if (text == null) return 0;
		if (stringToFind == null) return 0;
		
		Pattern p = Pattern.compile(Pattern.quote(stringToFind));
		Matcher m = p.matcher(text);
		int count = 0;
		while (m.find()) { count += 1; }
		return count;
	}

    /**
     * Find nth char in a text.
     * @return -1 when not found or when nth is 0 or negative, otherwise the position in the text (zero based)
     */
    public static int findNthChar(String text, char charToFind, int nth) {
        if (text == null) { return -1; }
        if (nth < 1) { return -1; }

        int count = 0;
        int pos = 0;

        while (pos < text.length()) {
            if (text.charAt(pos) == charToFind) {
                count++;
                if (count == nth) {
                    break;
                }
            }
            pos++;
        }
        return count < nth ? -1 : pos;
	}

	public static String stripToSemicolon(String toStrip) {
        if (toStrip == null) { return null; }
		int semiColonIdx = toStrip.indexOf(':');
		return semiColonIdx == -1 ? toStrip : toStrip.substring(semiColonIdx + 1).trim();  
	}

	/**
	 * @return true when aString is null or empty or has only whitespace.
	 */
    public static boolean isEmptyString(String aString) {
        return aString == null || aString.trim().isEmpty();
    }

	public static String recreateCommandLine(String[] args) {
		String separator = " ";

		if (args.length == 0) {
			return "";
		}
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : args) {
			if (string.contains(" ") || string.contains("%")) {
				stringBuilder.append("\"").append(string).append("\"");
			}
			else {
				stringBuilder.append(string);
			}
			stringBuilder.append(separator);
		}
		int lastIndex = stringBuilder.length();
		stringBuilder.delete(lastIndex - separator.length(), lastIndex);
		return stringBuilder.toString();
	}

	public static String useDefaultOrGivenValue(final String defaultValue, final String givenPattern) {
        return givenPattern == null ? defaultValue : givenPattern;
    }

	/**
	 * Insert single quote for texts that are formulas in excel.
	 * E.g. starts with a "-".
	 */
	public static String excelProofText(String text) {
        if (text == null) return null;
        return text.startsWith("-") ? '\'' + text : text;
    }
}

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
package nl.stokpop.lograter.util;

import nl.stokpop.lograter.LogRaterException;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Replace common found structures in (log) lines, such as IP numbers, domain names and URLs.
 */
public class InLineReplacer {

    public static final Function<String, String> THREE_LETTER_HASH =
            s -> s.length() > 0 ? ThreeLetterWords.getThreeLetterHashSameCapitalizationOrDigits(s) : s;

    public static final Function<String, String> FIRST_LETTER =
            s -> s.length() > 1 ? s.substring(0, 1) : s;

    private static final String MAPPER_FILE_SEP = "###";

    private static final String REGEXP_IP = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}((?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))";
    private static final String REGEXP_REQUEST_URL = "( )(/.*?)( HTTP/)";
    // first a letter to not match on version numbers
    private static final String REGEXP_DOMAIN = "([a-zA-Z][a-zA-Z0-9-.]*)\\.[a-zA-Z0-9-]+\\.[a-zA-Z0-9-]+";
    // the third group should be replaced, that is the path of the URL
    private static final String REGEXP_DOMAIN_WITH_URL = "(.)(http[s]{0,1}://[a-zA-Z][a-zA-Z0-9-.]*\\.[a-zA-Z0-9-]+\\.[a-zA-Z0-9-]+)(/.*?)\\1";

    // seems to end with base64 padding most of the times: ==
    private static final String REGEXP_ENCRYPTED_VALUE = "(.)([a-zA-Z0-9+-/]*?)(==\\1)";

    private static final Pattern PATTERN_IP = Pattern.compile(REGEXP_IP);
    private static final Pattern PATTERN_REQUEST_URL = Pattern.compile(REGEXP_REQUEST_URL);
    private static final Pattern PATTERN_DOMAIN = Pattern.compile(REGEXP_DOMAIN);
    private static final Pattern PATTERN_DOMAIN_WITH_URL = Pattern.compile(REGEXP_DOMAIN_WITH_URL);
    private static final Pattern PATTERN_ENCRYPTED_VALUE = Pattern.compile(REGEXP_ENCRYPTED_VALUE);

    private static final Pattern PATTERN_MAPPER_FILE_SEP = Pattern.compile(MAPPER_FILE_SEP);
    private static final Pattern PATTERN_DOLLAR_NUMBER = Pattern.compile("$\\d+");
    private static final Pattern PATTERN_DOT_STAR = Pattern.compile(Pattern.quote(".*"));
    private static final Pattern PATTERN_WORDS = Pattern.compile("\\b(.+?)\\b");
    private static final Pattern PATTERN_QUESTION_MARK = Pattern.compile("\\?");
    private static final Pattern PATTERN_SLASH = Pattern.compile("/");


    private static final Pattern PATTERN_CAMEL_CASE_SPLITTER = Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");

    /**
     * Replace (base64?) encoded encrypted values.
     */
    public static String replaceEncryptedValue(String text, Function<String, String> replacer) {
        Matcher matcher = PATTERN_ENCRYPTED_VALUE.matcher(text);
        return replaceAll(matcher, replacer);
    }
    
    /**
     * Replaces IP v4 addresses with numbers. Uses one capture group: the last number. So replace with e.g. 0.0.0.$1
     */
    public static String replaceIPv4(String text, String replacement) {
        return PATTERN_IP.matcher(text).replaceAll(replacement);
    }

    /**
     * Replaces domains with urls, the url is being replaced by three letter word hashes.
     * This is mostly for the referer field. Note that also parameters can be present after a ?.
     * Note: does not work if port number is present after domain. Does not work with IP addresses with numbers.
     */
    public static String replaceDomainWithUrl(String text, Function<String, String> replacer) {
        Matcher matcher = PATTERN_DOMAIN_WITH_URL.matcher(text);
        return replaceAllWithFunctionThirdGroup(matcher, s -> replaceUrlPathAfterSlashes(s, replacer));
    }

    /**
     * Replaces the URL in http request part, e.g. /my/url in "GET /my/url.html HTTP/1.1" with "/foo/bar.html".
     */
    public static String replaceUrlInRequestTriplet(String text, Function<String, String> replacer) {
        Matcher matcher = PATTERN_REQUEST_URL.matcher(text);
        // java 9+: Matcher.replaceAll???(Function<MatchResult,String> replacer)
        return replaceAll(matcher, s -> replaceUrlPathAfterSlashes(s, replacer));
    }

    /**
     * Replaces domain names, first part gets three letter hash, e.g. myserver.example.com becomes foo.newdomein.tld
     */
    public static String replaceDomains(String text, String newDomain, Function<String, String> replacer) {
        Matcher matcher = PATTERN_DOMAIN.matcher(text);
        return replaceAll(matcher, s -> replacer.apply(s) + "." + newDomain);
    }

    private static String replaceBeforeDot(String text, Function<String, String> replacer) {
        if (text.contains(".")) {
            int lastIndex = text.lastIndexOf('.');
            String beforeDot = text.substring(0,lastIndex);
            String afterDot = text.substring(lastIndex);
            return replacer.apply(beforeDot) + replaceUrlParamsAfterQuestionMark(afterDot, replacer);
        }
        return text.length() > 0 ? replacer.apply(text) : text;
    }

    private static String replaceUrlParamsAfterQuestionMark(String text, Function<String, String> replacer) {
        if (text.contains("?")) {
            int lastIndex = text.lastIndexOf('?');
            String beforeQuestionMark = text.substring(0,lastIndex);
            String afterQuestionMark = text.substring(lastIndex);
            String replacedParams = replaceParams(afterQuestionMark, replacer);
            return beforeQuestionMark + "?" + replacedParams;
        }
        else {
            return text;
        }
    }

    private static String replaceParams(String params, Function<String, String> replacer) {
        String[] amps = params.split("&");
        return Arrays.stream(amps).map(param -> replaceParam(param, replacer)).collect(Collectors.joining("&"));
    }

    private static String replaceParam(String param, Function<String, String> replacer) {
        return Arrays.stream(param.split("="))
                .map(replacer)
                .collect(Collectors.joining("="));
    }

    /**
     * Only works on regexps with three groups, e.g. (part1)(part2)(part3).
     * The middle part is replaced by the function result: part1FUNCTIONRESULTpart3.
     */
    private static String replaceAll(Matcher matcher, Function<String, String> replacer) {
        int groupCount = matcher.groupCount();

        // need to use StringBuffer here :-(
        StringBuffer result = new StringBuffer(256);
        while (matcher.find()) {
            switch (groupCount) {
                case 1:
                    String replacementOne = replacer.apply(matcher.group(1));
                    matcher.appendReplacement(result, Matcher.quoteReplacement(replacementOne));
                    break;
                case 3:
                    String replacementMiddle = replacer.apply(matcher.group(2));
                    // need add the parts outside of the group again, could also use two more groups and $1 and $3
                    // also make sure $ is escaped in replacement
                    matcher.appendReplacement(result, "$1" + Matcher.quoteReplacement(replacementMiddle) + "$3");
                    break;
                default:
                    throw new LogRaterException("Unexpected group count in regexp matcher (expected 1 or 3): " + matcher);
            }
        }
        return matcher.appendTail(result).toString();
    }

    /**
     * Only works on regexps with three groups and a repeated part1, e.g. (part1)(part2)(part3)(part1).
     * The middle part is replaced by the function result: part1part2FUNCTIONRESULTpart1.
     */
    private static String replaceAllWithFunctionThirdGroup(Matcher matcher, Function<String,String> function) {
        int groupCount = matcher.groupCount();
        if (groupCount != 3) {
            throw new LogRaterException("Unexpected group count in regexp matcher (expected 3): " + matcher);
        }
        // need to use StringBuffer here :-(
        StringBuffer result = new StringBuffer(256);
        while (matcher.find()) {
            String replacementMiddle = function.apply(matcher.group(3));
            // need add the parts outside of the group again, could also use two more groups and $1 and $3
            // also make sure $ is escaped in replacement
            matcher.appendReplacement(result, "$1" + "$2" + Matcher.quoteReplacement(replacementMiddle) + "$1");
        }
        return matcher.appendTail(result).toString();
    }

    /**
     * @return for input "/api/service/call.htm" returns "/foo/bar/zoo.htm"
     */
    public static String replaceUrlPathAfterSlashes(String urlPath, Function<String, String> replacer) {
        // skip small entries, such as '-'
        if (urlPath.length() < 2) {
            return urlPath;
        }
        String prefix = "";
        String postfix = urlPath;
        String domain = "";
        if (urlPath.startsWith("http://")) {
            prefix = "http://";
            postfix = urlPath.substring("http://".length());
        }
        else if (urlPath.startsWith("https://")) {
            prefix = "https://";
            postfix = urlPath.substring("https://".length());
        }

        int indexOfFirstSlash = postfix.indexOf('/');
        if (indexOfFirstSlash != -1) {
            String possibleDomain = postfix.substring(0, indexOfFirstSlash);
            if (StringUtils.countOccurrences(possibleDomain, ".") > 1) {
                domain = InLineReplacer.replaceIPorDomain(possibleDomain, "1.2.3.$1", replacer);
                postfix = postfix.substring(possibleDomain.length());
            }
        }
        Function<String, String> replacerInner = s -> replaceBeforeDot(s, replacer);
        return prefix + domain + replaceTextsWithinSeparator(postfix, "/", replacerInner);
    }

    private static String replaceIPorDomain(String domain, String ipReplacement, Function<String, String> replacer) {
        if (REGEXP_IP.matches(domain)) {
            return replaceIPv4(domain, ipReplacement);
        }
        else {
             return replaceWords(domain, replacer);
        }
    }

    /**
     * Make sure the replacer deals with empty String to return empty String!
     */
    public static String replaceTextsWithinSeparator(String text, String separator, Function<String, String> replacer) {
        String prefix = text.startsWith(separator) ? separator : "";
        // skip the first empty string in the splitAsStream below
        if (prefix.length() != 0) {
            text = text.substring(separator.length());
        }
        String postfix = text.endsWith(separator) ? separator : "";
        return Pattern.compile(Pattern.quote(separator)).splitAsStream(text)
                .map(replacer)
                .collect(Collectors.joining(separator, prefix, postfix));
    }

    /**
     * @return replaced obfuscate words with three letter hash word (replaces complete words only).
     */
    public static String replaceObfuscateWords(String text, List<String> obfuscateWords, Function<String, String> replacer) {
        String newLine = text;
        for (String word : obfuscateWords) {
            // use word boundaries \\b to match complete words only
            newLine = newLine.replaceAll("(?i)\\b" + word + "\\b", replacer.apply(word));
        }
        return newLine;
    }

    /**
     * Waiting for java 11 Predicate::not
     */
    public static <T> Predicate<T> not(Predicate<T> t) {
        return t.negate();
    }

    /**
     * Replaces words in a mapper line, like ^/section/(this)/url.*###section $1
     */
    public static String replaceWordsMapperLine(String line, Function<String, String> replacer) {
        String[] split = PATTERN_MAPPER_FILE_SEP.split(line);
        if (split.length != 2) {
            throw new LogRaterException(String.format("Separator pattern (%s) seems missing: %s", PATTERN_MAPPER_FILE_SEP, line));
        }
        return InLineReplacer.replaceMapperUrl(split[0], replacer) + MAPPER_FILE_SEP + InLineReplacer.replaceMapperDestination(split[1], replacer);
    }
    
    /**
     * @return line with all words (= contains at least one letter) with given separator,
     * replaced by three letter word based on hash.
     */
    private static String replaceMapperUrl(String line, Function<String, String> replacer) {
        // check for parameters in line
        if (PATTERN_QUESTION_MARK.matcher(line).find()) {
            String[] split = PATTERN_QUESTION_MARK.split(line);
            String replacedParams = replaceParams(split[1], replacer);
            String replacedUrl = PATTERN_SLASH.splitAsStream(split[0])
                    .map(s -> replaceMapperUrlWord(s, replacer))
                    .collect(Collectors.joining("/"));
            return replacedUrl + "\\?" + replacedParams;
        }
        else {
            return PATTERN_SLASH.splitAsStream(line)
                    .map(s -> replaceMapperUrlWord(s, replacer))
                    .collect(Collectors.joining("/"));
        }
    }

    /**
     * @return mapper destination (after ###) replaced words (= contains at least one letter)
     */
    private static String replaceMapperDestination(String destination, Function<String, String> replacer) {
        return Arrays.stream(destination.split(" "))
                .map(s -> replaceMapperDestinationWord(s, replacer))
                .collect(Collectors.joining(" "));
    }

    private static boolean containsLetter(String text) {
        return text.chars().anyMatch(Character::isLetter);
    }

    private static String replaceMapperUrlWord(String text, Function<String, String> replacer) {
        boolean addToEnd = false;
        if (".*".equals(text)) return text;
        if (text.contains(".*")) {
            // split and joining seems asymmetric if it ends with .*
            if (text.endsWith(".*")) addToEnd = true;
            return PATTERN_DOT_STAR.splitAsStream(text)
                    .map(s -> InLineReplacer.replaceIfWord(s, replacer))
                    .collect(Collectors.joining(".*")) + (addToEnd ? ".*" : "");
        }
        else {
            return replaceIfWord(text, replacer);
        }
    }

    private static String replaceMapperDestinationWord(String text, Function<String, String> replacer) {
        if (PATTERN_DOLLAR_NUMBER.matcher(text).matches()) return text;
        return replaceIfWord(text, replacer);
    }

    private static String replaceIfWord(String text, Function<String, String> replacer) {
        return containsLetter(text) ? replacer.apply(text) : text;
    }

    private static String replaceIfWord(String text, List<String> whiteList, Function<String, String> replacer) {
        return !whiteList.contains(text) ? replacer.apply(text) : text;
    }

    public static String replaceCamelCaseWords(String text, Function<String, String> replacer) {
        return PATTERN_CAMEL_CASE_SPLITTER.splitAsStream(text)
                .map(replacer)
                .collect(Collectors.joining());
    }
    /**
     * Replace all words that are separated by the separator. Do not replace words in the whitelist.
     */
    public static String replaceSeparatedWords(String text, String separator, List<String> whiteList, Function<String, String> replacer) {
        return Pattern.compile(Pattern.quote(separator)).splitAsStream(text)
                .map(s -> InLineReplacer.replaceIfWord(s, whiteList, replacer))
                .collect(Collectors.joining(separator));
    }
    
    public static String replaceWords(String text, Function<String, String> replacer) {
        // need to use StringBuffer here :-(
        StringBuffer output = new StringBuffer(256);
        Matcher matcher = PATTERN_WORDS.matcher(text);
        while (matcher.find()) {
            String toReplace = matcher.group();
            if (allPunctuation(toReplace)) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(toReplace));
            }
            else {
                matcher.appendReplacement(output, replacer.apply(toReplace));
            }
        }
        matcher.appendTail(output);
        return output.toString();
    }

    public static boolean allPunctuation(String text) {
        return text.chars().allMatch(c -> ",.;:/ []{}=-+_()!\\<>?*&^%$#@|'\"????~`".indexOf(c) != -1);
    }

    public static String replaceClassname(String classname, Function<String, String> replacer) {
        return Pattern.compile(Pattern.quote(".")).splitAsStream(classname)
                .map(s -> replaceCamelCaseWords(s, replacer))
                .collect(Collectors.joining("."));

    }
}

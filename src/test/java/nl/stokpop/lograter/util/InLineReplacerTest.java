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

import nl.stokpop.lograter.LogRaterException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class InLineReplacerTest {

    private static final Function<String, String> FIXED_REPLACER = s -> s.length() == 0 ? "" : "XYZ";

    @Test
    public void replaceIPv4() {
        assertEquals("0.0.0.4",
                InLineReplacer.replaceIPv4("1.2.3.4", "0.0.0.$1"));
    }

    @Test
    public void replaceRequestUrl() {
        assertEquals("GET /pah/sac/twa.html HTTP/1.1",
                InLineReplacer.replaceUrlInRequestTriplet("GET /api/service/one.html HTTP/1.1", InLineReplacer.THREE_LETTER_HASH));
    }

    @Test
    public void replaceAfterSlashTextWithFirstLetterOnly() {
        assertEquals("//f/b",
                InLineReplacer.replaceUrlPathAfterSlashes("//foo/bar", InLineReplacer.FIRST_LETTER));
    }


    @Test
    public void replaceDomains() {
        assertEquals("01/01/2020 kue.stokpop.nl 500 hex.stokpop.nl [INFO]",
                InLineReplacer.replaceDomains("01/01/2020 server.example.com 500 foo.another.nl [INFO]", "stokpop.nl", InLineReplacer.THREE_LETTER_HASH));
    }

    @Test
    public void replaceAfterSlashTextThreeLetterHash() {
        assertEquals("//nil/ooh",
                InLineReplacer.replaceUrlPathAfterSlashes("//fooz/barz", InLineReplacer.THREE_LETTER_HASH));
    }

    @Test
    public void replaceDomainWithUrl() {
        assertEquals("12/12/12 !https://a.b.c/nil/ooh.html?nay=cee&dup! some text",
                InLineReplacer.replaceDomainWithUrl("12/12/12 !https://a.b.c/fooz/barz.html?setting=something&nofilter! some text", InLineReplacer.THREE_LETTER_HASH));
    }

    @Test
    public void replaceDomainWithUrlHttp() {
        assertEquals("12/12/12 !http://a.b.c/nil/ooh.html?nay=cee&dup! some text",
                InLineReplacer.replaceDomainWithUrl("12/12/12 !http://a.b.c/fooz/barz.html?setting=something&nofilter! some text", InLineReplacer.THREE_LETTER_HASH));
    }

    @Test
    public void replaceObfuscateWords() {
        List<String> obfuscateList = Arrays.asList("some", "text");

        assertEquals("12/12/12 !https://a.b.c/nil/ooh.html?nay=cee&dup! dib law dib other law and textcase",
                InLineReplacer.replaceObfuscateWords("12/12/12 !https://a.b.c/nil/ooh.html?nay=cee&dup! some Text some other TEXT and textcase", obfuscateList, InLineReplacer.THREE_LETTER_HASH));
    }

    @Test
    public void replaceMapperUrl() {
        assertEquals("(.*)/gid/pen.*###hum $1 $12", InLineReplacer.replaceWordsMapperLine("(.*)/test/v1.*###session $1 $12", InLineReplacer.THREE_LETTER_HASH));
    }

    @Test
    public void replaceClassname() {
        assertEquals("las.kea.taw.pst.GutKeaLugGee", InLineReplacer.replaceClassname("nl.stokpop.lograter.util.InLineReplacerTest", InLineReplacer.THREE_LETTER_HASH));
    }

    @Test
    public void replaceWords() {
        String input = "bla bla [9438567948594753943945] what; is this=23";
        String expected = "kor kor [34] why; gip tsk=601";
        assertEquals(expected, InLineReplacer.replaceWords(input, InLineReplacer.THREE_LETTER_HASH));
    }

    @Test
    public void replaceUrlPathAfterSlashes() {
        assertEquals("https://XYZ.XYZ.XYZ/XYZ/XYZ.html?XYZ=XYZ&XYZ",
                InLineReplacer.replaceUrlPathAfterSlashes("https://a.b.c/nil/ooh.html?nay=cee&dup", FIXED_REPLACER));
    }

    @Test
    public void replaceTextsWithinSlashes() {
        assertEquals("/XYZ/XYZ", InLineReplacer.replaceTextsWithinSeparator("/health/ok", "/", FIXED_REPLACER));
        assertEquals("/XYZ/XYZ/", InLineReplacer.replaceTextsWithinSeparator("/health/ok/", "/", FIXED_REPLACER));
        assertEquals("/XYZ", InLineReplacer.replaceTextsWithinSeparator("/health", "/", FIXED_REPLACER));
        assertEquals("/XYZ/", InLineReplacer.replaceTextsWithinSeparator("/health/", "/", FIXED_REPLACER));
        assertEquals("&$%^asjh*^%XYZ&$%^asjh*^%XYZ&$%^asjh*^%", InLineReplacer.replaceTextsWithinSeparator("&$%^asjh*^%health&$%^asjh*^%ok&$%^asjh*^%", "&$%^asjh*^%", FIXED_REPLACER));
    }

    @Test(expected = LogRaterException.class)
    public void replaceWordsMapperLineMissingSep() {
        // separator is missing, exception expected
        InLineReplacer.replaceWordsMapperLine("/a/b/be-li", FIXED_REPLACER);
    }

    @Test
    public void replaceWordsMapperLine() {
        String line = InLineReplacer.replaceWordsMapperLine("/a/b/be-li###be-li", FIXED_REPLACER);
        assertEquals("/XYZ/XYZ/XYZ###XYZ", line);
    }


}
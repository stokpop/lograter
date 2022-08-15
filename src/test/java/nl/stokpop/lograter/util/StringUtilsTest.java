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

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testCountOccurrences() {
        Assert.assertEquals(4, StringUtils.countOccurrences("sdlkfjs    ldkjlkasdjflkasdf", 'd'));
        Assert.assertEquals(0, StringUtils.countOccurrences("", 'd'));
        Assert.assertEquals(0, StringUtils.countOccurrences(null, 'd'));
    }

    @Test
    public void testCountOccurrencesString() {
        Assert.assertEquals(3, StringUtils.countOccurrences("helloslkhellodjladfjhello", "hello"));
        Assert.assertEquals(1, StringUtils.countOccurrences("hello", "hello"));
        Assert.assertEquals(0, StringUtils.countOccurrences("", "hello"));
        Assert.assertEquals(0, StringUtils.countOccurrences(null, "hello"));
        Assert.assertEquals(0, StringUtils.countOccurrences("hello", null));
        Assert.assertEquals(6, StringUtils.countOccurrences("helloslkhellodjladfjhello\nhelloslkhellodjladfjhello", "hello"));
    }

    @Test
    public void testNthChar() {
        Assert.assertEquals(0 ,StringUtils.findNthChar("gfhjsdkjfhasdkjfhslakhf", 'g', 1));
        Assert.assertEquals(1 ,StringUtils.findNthChar("gfhjsdkjfhasdkjfhslakhf", 'f', 1));
        Assert.assertEquals(-1 ,StringUtils.findNthChar("gfhjsdkjfhasdkjfhslakhf", 'f', -1));
        Assert.assertEquals(-1 ,StringUtils.findNthChar("gfhjsdkjfhasdkjfhslakhf", 'f', 0));
        Assert.assertEquals(8 ,StringUtils.findNthChar("gfhjsdkjfhasdkjfhslakhf", 'f', 2));
        Assert.assertEquals(15 ,StringUtils.findNthChar("gfhjsdkjfhasdkjfhslakhf", 'f', 3));
        Assert.assertEquals(-1 ,StringUtils.findNthChar("gfhjsdkjfhasdkjfhslakhf", 'x', 1));
        Assert.assertEquals(-1 ,StringUtils.findNthChar(null, 'x', 2));
    }

    @Test
    public void testStripToSemicolon() {
        Assert.assertEquals("dffgsdfgsdf", StringUtils.stripToSemicolon("3479348729:dffgsdfgsdf"));
        Assert.assertEquals("dffgsdfgsdf", StringUtils.stripToSemicolon("dffgsdfgsdf"));
    }

    @Test
    public void testIsEmptyString() {
        Assert.assertTrue(StringUtils.isEmptyString(""));
        Assert.assertTrue(StringUtils.isEmptyString(null));
        Assert.assertTrue(StringUtils.isEmptyString("   "));
        Assert.assertTrue(StringUtils.isEmptyString("   "));
        Assert.assertFalse(StringUtils.isEmptyString("  x   "));
    }
}

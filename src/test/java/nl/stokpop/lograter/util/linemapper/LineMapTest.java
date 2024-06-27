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
package nl.stokpop.lograter.util.linemapper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LineMapTest {

    @Test
    public void testIsUrlMatch() {
        LineMap lineMap = new LineMap(".*bla.*", "bla mapper");

        assertTrue("is a match", lineMap.isMatch("vlablavla"));
        assertFalse("is not a match", lineMap.isMatch("vlavlavla"));
    }

    @Test
    public void testMatchWithReplaceGroup() {
        LineMap lineMap = new LineMap("(.*)bla(.*)", "$1 $2 mapper");

        String line = "doesblamatch";
        assertTrue("this is a match", lineMap.isMatch(line));
        assertEquals("does match mapper", lineMap.getNameWithReplacementsFromLine(line));
    }

    @Test
    public void testMatchWithNoReplaceGroup() {
        String expected = "$1 $2 mapper";
        LineMap lineMap = new LineMap(".*bla.*", expected);

        String line = "doesblamatch";
        assertTrue("this is a match", lineMap.isMatch(line));
        assertEquals(expected, lineMap.getNameWithReplacementsFromLine(line));
    }

    @Test
    public void testMatchWithReplaceGroupAndNotEnoughReplacementVariables() {
        LineMap lineMap = new LineMap("(.*)(bla_)?bar(.*)", "$1 $3 mapper");

        String line = "doesbarmatch";
        assertTrue("this is a match", lineMap.isMatch(line));
        assertEquals("does match mapper", lineMap.getNameWithReplacementsFromLine(line));
    }

    @Test
    public void testMatchWithReplaceGroupNoSpaces() {
        LineMap lineMap = new LineMap("(.*)(bla_)?bar(.*)", "$1$3mapper");

        String line = "doesbarmatch";
        assertTrue("this is a match", lineMap.isMatch(line));
        assertEquals("doesmatchmapper", lineMap.getNameWithReplacementsFromLine(line));
    }

    @Test
    public void testMatchWithReplaceGroupEscapeAndLetters() {
        String expected = "$1tmapper";
        LineMap lineMap = new LineMap("(.*)(bla_)?bar(.*)", "\\$1\\tmapper");

        String line = "doesbarmatch";
        assertTrue("this is a match", lineMap.isMatch(line));
        assertEquals(expected, lineMap.getNameWithReplacementsFromLine(line));
    }

	@Test
	public void testMatchWithReplaceGroupCase1() {
		LineMap lineMap = new LineMap("^/services/(.*)/v(1|2)(|/)$", "RBO - Services $1");

		String line = "/services/test/v1";
		assertTrue("this is a match", lineMap.isMatch(line));
		assertEquals("RBO - Services test", lineMap.getNameWithReplacementsFromLine(line));
	}

}
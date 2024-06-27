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
package nl.stokpop.lograter.util.time;

import org.junit.Test;

import static org.junit.Assert.*;

public class DateUtilsTest {

    @Test
    public void testIsValidDateTimeString() {
        assertTrue("is valid date string", DateUtils.isValidDateTimeString("20150301T111213"));
        assertFalse("is not a valid date string, without seconds", DateUtils.isValidDateTimeString("20150301T1112"));
        assertFalse("T is missing", DateUtils.isValidDateTimeString("20150301111213"));
        assertFalse("not a valid date", DateUtils.isValidDateTimeString("2015 maart april"));
    }

    @Test
    public void testStrftimePatternToDateTimeFormatterPattern() {
        assertEquals("dd/MMM/yyyy:HH:mm:ss", DateUtils.convertStrfTimePatternToDateTimeFormatterPattern("%d/%b/%Y:%H:%M:%S"));
        assertEquals("dd/MMM/yyyy:HH:mm:ss", DateUtils.convertStrfTimePatternToDateTimeFormatterPattern("%d/%b/%Y:%T"));
    }

    @Test
    public void parseISOTimeTest() {
        assertEquals(1572877119000L, DateUtils.parseISOTime("2019-11-04T15:18:39"));
    }

}
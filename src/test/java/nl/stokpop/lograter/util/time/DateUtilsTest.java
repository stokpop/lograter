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
package nl.stokpop.lograter.util.time;

import org.junit.Assert;
import org.junit.Test;

public class DateUtilsTest {

    @Test
    public void testIsValidDateTimeString() {
        Assert.assertTrue("is valid date string", DateUtils.isValidDateTimeString("20150301T111213"));
        Assert.assertFalse("is not a valid date string, without seconds", DateUtils.isValidDateTimeString("20150301T1112"));
        Assert.assertFalse("T is missing", DateUtils.isValidDateTimeString("20150301111213"));
        Assert.assertFalse("not a valid date", DateUtils.isValidDateTimeString("2015 maart april"));
    }

    @Test
    public void testStrftimePatternToDateTimeFormatterPattern() {
        Assert.assertEquals("dd/MMM/yyyy:HH:mm:ss", DateUtils.convertStrfTimePatternToDateTimeFormatterPattern("%d/%b/%Y:%H:%M:%S"));
        Assert.assertEquals("dd/MMM/yyyy:HH:mm:ss", DateUtils.convertStrfTimePatternToDateTimeFormatterPattern("%d/%b/%Y:%T"));
    }

}
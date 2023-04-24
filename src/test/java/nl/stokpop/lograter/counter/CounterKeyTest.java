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
package nl.stokpop.lograter.counter;

import junit.framework.TestCase;

import java.util.*;

import static java.util.Collections.*;
import static nl.stokpop.lograter.logentry.LogEntry.HTTP_METHOD;
import static nl.stokpop.lograter.logentry.LogEntry.HTTP_STATUS;

public class CounterKeyTest extends TestCase {

    public void testCompareTo() {
        assertEquals(-1, CounterKey.of("a").compareTo(CounterKey.of("b")));
        assertEquals(0, CounterKey.of("a").compareTo(CounterKey.of("a")));
        assertEquals(1, CounterKey.of("b").compareTo(CounterKey.of("a")));

        CounterKeyMetaData metaOne = new CounterKeyMetaData(singletonList(HTTP_STATUS), singletonList("200"));
        CounterKeyMetaData metaTwo = new CounterKeyMetaData(singletonList(HTTP_STATUS), singletonList("500"));

        // no fields in compare
        assertEquals(0, CounterKey.of("a", metaOne).compareTo(CounterKey.of("a", metaTwo)));
    }

    public void testMerge() {
        List<String> one1 = new ArrayList<>();
        one1.add(HTTP_STATUS);
        one1.add(HTTP_METHOD);
        List<String> two1 = new ArrayList<>();
        two1.add("200");
        two1.add("GET");
        CounterKey k1 = CounterKey.of("foo", new CounterKeyMetaData(one1, two1));

        List<String> one2 = new ArrayList<>();
        one2.add(HTTP_STATUS);
        one2.add(HTTP_METHOD);
        List<String> two2 = new ArrayList<>();
        two2.add("500");
        two2.add("GET");
        CounterKey k2 = CounterKey.of("bar", new CounterKeyMetaData(one2, two2));

        CounterKey merged = CounterKey.merge(k1, k2);

        assertEquals("foo-bar-merged", merged.getName());
        assertEquals("200+500", merged.getMetaData().get(HTTP_STATUS));
        assertEquals("GET", merged.getMetaData().get(HTTP_METHOD));
    }
}
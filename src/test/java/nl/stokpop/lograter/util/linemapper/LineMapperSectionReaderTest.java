/*
 * Copyright (C) 2020 Peter Paul Bakker, Stokpop Software Solutions
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LineMapperSectionReaderTest {

    @Test
    public void testInitializeMapperTables() throws IOException {

        String mapperConfig =
                ".*bla.*###BLA line detected\n" +
                ".fla[b]{1,10}/###fla line detected\n" +
                "section(foo)\n" +
                ".*foo.*###FOO total\n" +
                ".*bar(.*)###bar \\1";
        LineMapperReader reader = new LineMapperReader();
        InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(mapperConfig.getBytes(StandardCharsets.UTF_8)));
        List<LineMapperSection> lineMapperSections = reader.initializeMapperTables(input);

        assertEquals("Check mapper size", 2, lineMapperSections.size());
        LineMapperSection lineMapperSection1 = lineMapperSections.get(0);
        assertEquals("Check first line mapper name", "default", lineMapperSection1.getName());
        assertEquals(("Check lineMaps"), 2, lineMapperSection1.size());
        LineMapperSection lineMapperSection2 = lineMapperSections.get(1);
        assertEquals("Check second line mapper name", "foo", lineMapperSection2.getName());
        assertEquals(("Check lineMaps"), 2, lineMapperSection2.size());
    }

    @Test
    public void testInitializeFourMappersInDefaultTable() throws IOException {

        String mapperConfig =
                        "one_regexp###one_name\n" +
                        "two_regexp###two_name\n" +
                        "three_regexp###three_name\n" +
                        "four_regexp###four_name";
        LineMapperReader reader = new LineMapperReader();
        InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(mapperConfig.getBytes(StandardCharsets.UTF_8)));
        List<LineMapperSection> mappers = reader.initializeMapperTables(input);
        assertEquals("Check line mappers size", 1, mappers.size());
        LineMapperSection lineMapper = mappers.get(0);
        assertEquals("Check first line mapper name", "default", lineMapper.getName());
        assertEquals("Check first mapper", 4, lineMapper.size());
    }


}

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
package nl.stokpop.lograter.jmx.os.parse;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import nl.stokpop.lograter.jmx.os.JmxOperatingSystemMetrics;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.stream.Stream;


@Slf4j
public enum JmxOperatingSystemCsvFileParser {
    INSTANCE;

    private static final char SEPARATOR = ',';

    public Stream<JmxOperatingSystemMetrics> parse(File file) {
        Reader fileReader = getFileReader(file);

        return new CsvToBeanBuilder<JmxOperatingSystemMetrics>(fileReader)
                .withType(JmxOperatingSystemMetrics.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withSeparator(SEPARATOR)
                .build()
                .stream();
    }

    private Reader getFileReader(File file) {
        try {
            return Files.newBufferedReader(file.toPath());
        } catch (IOException e) {
            String errorMessage = "Cannot open input file " + file;
            log.error(errorMessage);
            throw new JmxOperatingSystemCsvFileParseException(errorMessage, e);
        }
    }
}

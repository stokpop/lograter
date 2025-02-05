/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.jmx.memory.parse;

import lombok.extern.slf4j.Slf4j;
import nl.stokpop.lograter.gc.GcLogParseException;
import nl.stokpop.lograter.jmx.memory.algorithm.GcAlgorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Slf4j
public enum GcAlgorithmDetector {
    INSTANCE;

    public GcAlgorithm detect(File file) {
        String header = readHeaderLine(file);
        return findGcAlgorithm(header, file);
    }

    private GcAlgorithm findGcAlgorithm(String header, File file) {
        Optional<GcAlgorithm> gcAlgorithm = GcAlgorithm.forPattern(header);
        if (!gcAlgorithm.isPresent()) {
            String errorMessage = "Header line '" + header + "' of file " + file + " does not match with existing patterns.";
            log.error(errorMessage);
            throw new GcLogParseException(errorMessage);
        }

        return gcAlgorithm.get();
    }

    private String readHeaderLine(File file) {
        Optional<String> headerLine;
        try {
            headerLine = Files.lines(file.toPath()).findFirst();
            if (!headerLine.isPresent()) {
                String errorMessage = "File " + file + "is empty.";
                log.error(errorMessage);
                throw new GcLogParseException(errorMessage);
            }
        } catch (IOException e) {
            String errorMessage = "Error while reading file " + file ;
            log.error(errorMessage);
            throw new GcLogParseException(errorMessage, e);
        }
        return headerLine.get();
    }
}
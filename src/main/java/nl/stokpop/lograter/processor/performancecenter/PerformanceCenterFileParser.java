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
package nl.stokpop.lograter.processor.performancecenter;

import nl.stokpop.lograter.LogRaterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Parse config and data files from a Performance Center Results.zip.
 */
public class PerformanceCenterFileParser {

    private static final Logger log = LoggerFactory.getLogger(PerformanceCenterFileParser.class);

    /**
     * Find the .lra file and get the aggregation period in seconds (AggSecGran).
     *
     * @param unzippedResultsDir an unzipped Results.zip directory
     * @return 0 if not found, the aggregation period in seconds if found
     * @throws IOException if the .lra file is not found or cannot be read
     */
    public static int fetchAnalysisAggregationPeriodInSeconds(File unzippedResultsDir) throws IOException {

        // find the file that ends with lra
        File[] files = unzippedResultsDir.listFiles((dir, name) -> name.endsWith(".lra"));

        if (files == null || files.length == 0) {
            throw new IOException("No file found that ends in '.lra'");
        }

        if (files.length > 1) {
            log.warn("Multiple files found that end in '.lra', using the first one: {}", Arrays.toString(files));
        }

        File loadRunnerSettingFile = files[0];

        int aggregationInSec;

        try (Stream<String> lines = Files.lines(Paths.get(loadRunnerSettingFile.toURI()))) {
            String aggSecGranLine = lines.filter(line -> line.startsWith("AggSecGran")).findFirst().orElse("AggSecGran=0");
            String[] lineParts = aggSecGranLine.split("=");
            if (lineParts.length < 2) {
                throw new LogRaterException("Expected a name and value separated by = in this line: " + aggSecGranLine);
            }
            aggregationInSec = Integer.parseInt(lineParts[1]);
        }

        return aggregationInSec;
    }
}


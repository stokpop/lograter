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

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reads a mapper file and creates the mappers defined in that file.
 *
 * Can be multiple mappers, for multiple tables in the report.
 *
 */
public class LineMapperReader {

    private static final Logger log = LoggerFactory.getLogger(LineMapperReader.class);
    private static final Pattern MAPPER_SPLIT = Pattern.compile("###");

    public List<LineMapperSection> initializeMapperTables(InputStreamReader mapperConfigFile) throws IOException {
        return loadMappers(mapperConfigFile);
    }

    public List<LineMapperSection> initializeMappers(InputStream mapperConfigFile) throws IOException {
        return loadMappers(FileUtils.createBufferedInputStreamReader(mapperConfigFile));
    }

    public List<LineMapperSection> initializeMappers(File mapperFile) throws IOException {
        if (mapperFile == null) {
            throw new NullPointerException("Provide a mapper config file for LineMapper");
        }
        if (!mapperFile.exists()) {
            throw new LogRaterException("Mapper file not found: " + mapperFile.getPath());
        }
        log.info("Using line mapper config file: [{}]", mapperFile);

        List<LineMapperSection> mappers;
        try (BufferedReader reader = FileUtils.createBufferedReader(mapperFile)) {
            mappers = loadMappers(reader);
        }

        return mappers;
    }

    private List<LineMapperSection> loadMappers(InputStreamReader mappersReader) throws IOException {
        return loadMappers(new BufferedReader(mappersReader));
    }

    private List<LineMapperSection> loadMappers(BufferedReader mappersReader) throws IOException {

        List<LineMapperSection> lineMappers = new ArrayList<>();

        int linenr = 0;
        String line;
        int tableNumber = 0;

        LineMapperSection lineMapper = new LineMapperSection("default");

        while ((line = mappersReader.readLine()) != null) {
            linenr++;
            String trimmedLine = line.trim();
            if (trimmedLine.length() == 0) {
                continue;
            }

            try {
                if (trimmedLine.startsWith("#")) {
                    // skip: comment line
                    log.debug("Skipped comment line: {}", line);
                }
                else if (trimmedLine.toLowerCase().startsWith("section")) {
                    if (lineMapper.size() > 0 ) lineMappers.add(lineMapper);
                    String sectionName = "unnamed section " + tableNumber++;
                    int firstBracket = trimmedLine.indexOf('(');
                    if (firstBracket == -1) {
                        log.warn("Using section name: {}. Bracket '(' missing for section in linenr {}: {}", sectionName, linenr, line);
                    }
                    else if (trimmedLine.length() <= firstBracket + 1) {
                        log.warn("Using section name: {}. No section name after opening bracket in linenr {}: {}", sectionName, linenr, line);
                    }
                    else {
                        int secondBracket = trimmedLine.indexOf(')');
                        if (secondBracket == -1) {
                            log.warn("Using remainder of line as section name. Bracket ')' missing for section in linenr {}: {}", linenr, line);
                            sectionName = trimmedLine.substring(firstBracket + 1);
                        }
                        else {
                            sectionName = trimmedLine.substring(firstBracket + 1, secondBracket);
                        }
                    }
                    lineMapper = new LineMapperSection(sectionName);
                }
                else {
                    String[] parts = MAPPER_SPLIT.split(line);
                    if (parts.length != 2) {
                        throw new LogRaterException("Mapper line is missing '###' separator: " + line);
                    }
                    String regexp = parts[0];
                    String name = parts[1];
                    lineMapper.addMapperRule(regexp, name);
                }

            } catch (Exception e) {
                log.error("Error in line {} error: {} line: {}", linenr, e.getMessage(), line);
                throw new LogRaterException("Error reading mapper config", e);
            }

        }
        if (lineMapper.size() > 0) {
            lineMappers.add(lineMapper);
        }
        return lineMappers;
    }

}

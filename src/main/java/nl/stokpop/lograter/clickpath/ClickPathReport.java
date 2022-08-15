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
package nl.stokpop.lograter.clickpath;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.SimpleCounter;
import nl.stokpop.lograter.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Print out a clickpath report.
 */
public class ClickPathReport {
    public static void reportClickpaths(ClickPathCollector clickpathCollector, File clickPathFile, boolean reportStepDuration) {
        try (PrintWriter writer = FileUtils.createBufferedPrintWriterWithUTF8(clickPathFile)) {
            writer.write("COUNT, EXAMPLE SESSIONID, AVG SESSION DURATION, STEPS, CLICKPATH\n");
            for (Map.Entry<String, SimpleCounter> clickPath : clickpathCollector.getClickPaths().entrySet()) {
                final String path = clickPath.getKey();
                final String sessionId = clickpathCollector.getExampleSessionIdForClickPath(path);
                final long count = clickPath.getValue().getCount();
                final long avgDuration = clickpathCollector.getAvgSessionDurationForClickPath(path);
                final String pathToReport = reportStepDuration ? clickpathCollector.getPathAsStringWithAvgDuration(path) : path;
                final long length = clickpathCollector.getClickPathLength(path);
                writer.write(String.format("%s,%s,%s,%s, %s%n", count, sessionId, avgDuration, length, pathToReport));
            }
        } catch (IOException e) {
            throw new LogRaterException("Cannot write to file " + clickPathFile.getPath(), e);
        }
    }
}

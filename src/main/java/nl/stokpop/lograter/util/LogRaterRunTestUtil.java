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
package nl.stokpop.lograter.util;

import nl.stokpop.lograter.LogRater;
import nl.stokpop.lograter.command.LogRaterCommand;
import nl.stokpop.lograter.reportcreator.ReportCreator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * Utilities to perform unit tests on LogRater
 */
public class LogRaterRunTestUtil {

    public static String getOutputFromLogRater(String[] runArgs) throws IOException {
        return getOutputFromLogRater(runArgs, Collections.emptyMap());
    }

    public static String getOutputFromLogRater(String[] runArgs, Map<LogRaterCommand, ReportCreator> extraCommands) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(1024)) {
            try (PrintWriter printWriter = FileUtils.createBufferedPrintWriterWithUTF8(baos)) {
                new LogRater(printWriter).startLogRater(runArgs, extraCommands);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }

    /**
     * Turn a test resource into a File to use in tests.
     * @param classToUseForResource needed for the proper classloader
     * @param testResourcePath path to test resource (or any file on the classpath)
     * @return the test resource as File
     */
    public static File convertTestResourceIntoFile(Class<?> classToUseForResource, String testResourcePath) {
        if (!testResourcePath.startsWith("/")) {
            testResourcePath = "/" + testResourcePath;
        }
        URL url = classToUseForResource.getResource(testResourcePath);
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to create URI for " + url, e);
        }
    }
}

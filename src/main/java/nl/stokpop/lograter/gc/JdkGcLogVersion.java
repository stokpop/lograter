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
package nl.stokpop.lograter.gc;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public enum JdkGcLogVersion {

    WAS7, WAS8, OPENJDK_1_8, UNKNOWN;

    private static final String HTTP_WWW_IBM_COM_J9_VERBOSEGC = "http://www.ibm.com/j9/verbosegc";

    public static JdkGcLogVersion checkJdkVersion(File gcFile) throws IOException {

        try (BufferedReader gcFileInput = FileUtils.getBufferedReader(gcFile)) {
            String line;
            int lineNumber = 0;

            while ((line = gcFileInput.readLine()) != null) {

                final int NUMBER_OF_LINES_TO_CHECK_FOR_WEBSPHERE_VERSION = 10;

                lineNumber++;
                if (lineNumber > NUMBER_OF_LINES_TO_CHECK_FOR_WEBSPHERE_VERSION) {
                    throw new LogRaterException("Cannot determine Websphere version of verbose gc file, " +
                            "no gcverbose tag found in first " + NUMBER_OF_LINES_TO_CHECK_FOR_WEBSPHERE_VERSION + " lines of file " + gcFile);
                }
                if (line.contains("<verbosegc version=") && !line.contains(HTTP_WWW_IBM_COM_J9_VERBOSEGC)) {
                    return JdkGcLogVersion.WAS7;
                }
                if (line.contains("<verbosegc") && line.contains(HTTP_WWW_IBM_COM_J9_VERBOSEGC)) {
                    return JdkGcLogVersion.WAS8;
                }
                if (line.contains("HotSpot") && line.contains("(1.8")) {
                	return OPENJDK_1_8;
                }
            }
        }

        return JdkGcLogVersion.UNKNOWN;

    }
}

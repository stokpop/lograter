/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.report;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ReportWriter {

    private final static Logger log = LoggerFactory.getLogger(ReportWriter.class);

    public static File write(String outputFilename, File reportDirectory, LogReport report, TimePeriod analysisPeriod) throws IOException {

        final File outFile;

        final String filename = DateUtils.replaceTimestampMarkerInFilename(outputFilename);
        final File file = new File(filename);

        final File parent = file.getParentFile();
        if (parent == null) {
            outFile = new File(reportDirectory, filename);
        } else {
            if (!parent.exists()) {
                throw new LogRaterException("Path not found: " + parent);
            }
            outFile = file;
        }

        try (PrintWriter printWriter = FileUtils.createBufferedPrintWriterWithUTF8(outFile)) {
            write(printWriter, report, analysisPeriod);
        }

        log.info("Report: {}", outFile.getAbsolutePath());
        return outFile;
    }

    public static void write(PrintWriter printStream, LogReport report, TimePeriod analysisPeriod) throws IOException {
	    if (!analysisPeriod.hasBothTimestampsSet()) {
		    throw new LogRaterException("Do not supply a non-set analysis period to the report.");
	    }
        try {
            report.report(printStream, analysisPeriod );
        } finally {
            printStream.flush();
            printStream.close();
        }
    }

}

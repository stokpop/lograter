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
package nl.stokpop.lograter.reportcreator;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.command.CommandLargeAllocationsToCsv;
import nl.stokpop.lograter.command.CommandMain;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.was.LargeAllocation;
import nl.stokpop.lograter.was.NativeStdErrorParser;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * OpenJ9 native error log report to check large object allocations.
 */
public class LargeAllocationsReportCreator implements ReportCreatorWithCommand<CommandLargeAllocationsToCsv> {

	private static final Logger log = LoggerFactory.getLogger(LargeAllocationsReportCreator.class);

	private static final DateTimeFormatter DATE_TIME_FORMATTER =
			DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withLocale(new Locale("US"));

	@Override
	public void createReport(PrintWriter outputStream, CommandMain mainCommand, CommandLargeAllocationsToCsv cmdAllocations) throws IOException {
		List<String> filenames = cmdAllocations.files;
		if (filenames == null || filenames.size() == 0) {
			throw new LogRaterException("Please supply one or more gc verbose log filenames.");
		}

		File csvFile = FileUtils.createFullOutputReportPath(mainCommand.reportDirectory, cmdAllocations.csvFile);

		List<File> files = fromFilenamesToFiles(filenames);

		List<LargeAllocation> largeAllocations = NativeStdErrorParser.getLargeAllocationsFromFiles(files);

		log.info("Writing to csv file: {}", csvFile.getPath());


		try (
			OutputStream csvOutputStream = new BufferedOutputStream(new FileOutputStream(csvFile));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(csvOutputStream, StandardCharsets.UTF_8))
		) {
			writer.println("timestamp,bytes,type,threadname,code location");
			for (LargeAllocation alloc : largeAllocations) {
				StringBuilder line = new StringBuilder();
				String dateTimeString = DATE_TIME_FORMATTER.print(alloc.getTimestamp());
				line.append(dateTimeString).append(",");
				line.append(alloc.getBytes()).append(",");
				line.append(alloc.getType()).append(",");
				line.append(alloc.getThreadName()).append(",");
				line.append(alloc.getStackTraceFirstLine());
				writer.println(line);
			}
		}

		outputStream.printf("Check out result in csv file: %s", csvFile.getPath());
		outputStream.println();

	}

	private static List<File> fromFilenamesToFiles(List<String> filenames) {
		List<File> files = new ArrayList<>();
		for (String filename : filenames) {
			File file = new File(filename);
			if (!file.exists()) {
				throw new LogRaterException("File does not exist: " + file.getAbsolutePath());
			}
			files.add(file);
		}
		return files;
	}

}

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
package nl.stokpop.lograter.feeder;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.SimpleCounter;
import nl.stokpop.lograter.parser.ApplicationLogParser;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.LogRaterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class FileFeeder implements FeedProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(FileFeeder.class);
	private final Pattern filterPatternIncludes;
    private final Pattern filterPatternExcludes;
    private final int skipLines;
	private final List<File> files;

	private Map<String, SimpleCounter> exceptionsCounter = new HashMap<>();

	public FileFeeder(List<File> files) {
		this(files,null, (Pattern) null, 0);
	}

	public FileFeeder(List<File> files, int skipLines) {
		this(files, null, (Pattern) null, skipLines);
	}

    /**
     * Feed files line by line to a Feeder.
     * @param filterPatternIncludes regular expression to allowed lines (will be included)
     * @param filterPatternExcludes regular expression to denied lines (will be skipped)
     * @param skipLines number of lines to skip for each file (e.g. skip a header line)
     */
    public FileFeeder(List<File> files, final Pattern filterPatternIncludes, final Pattern filterPatternExcludes, final int skipLines) {
        this.filterPatternIncludes = filterPatternIncludes;
        this.filterPatternExcludes = filterPatternExcludes;
        this.skipLines = skipLines;
        this.files = Collections.unmodifiableList(new ArrayList<>(files));
    }

    public FileFeeder(List<File> files, final String filterRegexpIncludes, final String filterRegexpExcludes) {
        this(files, createPattern(filterRegexpIncludes), createPattern(filterRegexpExcludes), 0);
	}

    public FileFeeder(List<File> files, final String filterRegexpIncludes, final String filterRegexpExcludes, final int skipLines) {
        this(files, createPattern(filterRegexpIncludes), createPattern(filterRegexpExcludes), skipLines);
	}

    private static Pattern createPattern(final String regularExpression) {
        if (regularExpression == null) {
			return null;
		}
		else {
			return Pattern.compile(regularExpression);
		}
    }

    private void processLogFile(File file, Feeder feeder) throws IOException {

		if (!file.exists()) {
			log.error("File does not exist: {}", file);
			return;
		}
		if (file.isDirectory()) {
			log.error("Directory instead of file found, skipped: {}", file);
			return;
		}
		log.info("Start processing file: {}", file);

        try (BufferedReader br = FileUtils.createBufferedReader(file)) {

            String logline;
            long linenr = 0;

            while ((logline = br.readLine()) != null) {
                linenr++;
                if (linenr <= skipLines) {
                    logHeaderLine(logline, linenr);
                    continue;
                }
                if (logline.trim().length() == 0) {
                    continue;
                }
                try {
                    final boolean includeLine = filterPatternIncludes == null || filterPatternIncludes.matcher(logline).find();
                    final boolean excludeLine = filterPatternExcludes != null && filterPatternExcludes.matcher(logline).find();
                    if (log.isDebugEnabled()) {
                        if (filterPatternIncludes != null) log.debug("Log line {} included by filter pattern include '{}': {}", linenr, filterPatternIncludes, includeLine);
                        if (filterPatternExcludes != null) log.debug("Log line {} excluded by filter pattern exclude '{}': {}", linenr, filterPatternExcludes, excludeLine);
                    }
                    if (includeLine && !excludeLine) {
                        feeder.addLogLine(file.getName(), logline);
                    }
                } catch (Exception e) {
                    handleError(e, linenr, file, logline);
                }

            }
            // the application log feeder processes one minus current entry
            // to add non-loglines to correct entry
            if (feeder instanceof ApplicationLogParser) {
                ((ApplicationLogParser) feeder).processLastEntry();
            }
        }
	}

    private void logHeaderLine(final String logline, final long linenr) {
        // log up to 5 header lines on info, otherwise use debug to see all
        if (linenr < 5) {
            log.info("Skipping header line [{}/{}]: [{}]", linenr, skipLines, logline);
        }
        else {
            log.debug("Skipping header line [{}/{}]: [{}]", linenr, skipLines, logline);
        }
    }

    /**
	 * Potentially a lot of errors can occur when parsing fails, in worst case each line fails to parse.
	 * Only report first few occurrences, next, aggregate similar exceptions.
	 */
	private void handleError(Exception e, long linenr, File file, String logline) {
		String exceptionClassname = e.getClass().getName();

		if (exceptionsCounter.containsKey(exceptionClassname)) {
			exceptionsCounter.get(exceptionClassname).inc();
		}
		else {
			exceptionsCounter.put(exceptionClassname, new SimpleCounter(1));
		}

		long currentCount = exceptionsCounter.get(exceptionClassname).getCount();
		if (currentCount <= 20) {
			log.error("Error in line {} in file [{}] error: [{}] logline: {}", linenr, file, e, logline);
			if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
		}
		else {
			boolean isMod10Count = LogRaterUtils.isMod10Count(currentCount);
			if (isMod10Count) {
				log.error("Encountered exception [{}] in file [{}] in [{}] lines, now at line [{}].", exceptionClassname, file, currentCount, linenr);
			}
		}
	}

//	public void feedFilesAsString(List<String> files, Feeder feeder) {
//		feedFiles(FileUtils.findFilesThatMatchFilenames(files), feeder);
//	}

	public void feed(Feeder feeder) {

		if (files == null || files.size() == 0) {
			throw new LogRaterException("No files given to feeder.");
		}

		log.info("Using log files: {}", files);

		long startTime = System.currentTimeMillis();

		for (File logFile : files) {
			try {
				processLogFile(logFile, feeder);
			} catch (IOException e) {
                log.error("Cannot feed file: [" + logFile.getName() + "], skipping this file!", e);
			}
		}
		
		long endTime = System.currentTimeMillis();
		long durationInMillis = endTime - startTime;
		log.info("Processing time feeder: " + durationInMillis / 1000 + " seconds");
		
	}

	private void processDirectoryWithSubdirs(File dir, Feeder feeder) {

		if (!dir.isDirectory()) {
			throw new LogRaterException("Not a directory " + dir.getAbsolutePath());
		}

		File[] serverDirs = dir.listFiles();
		if (serverDirs == null || serverDirs.length == 0) {
			throw new LogRaterException("No subdirs found in " + dir.getAbsolutePath());
		}

		long startTime = System.currentTimeMillis();

		for (File serverDir : serverDirs) {

			File[] logFiles = serverDir.listFiles();

            if(logFiles == null) {
                throw new LogRaterException(
                        "Not a directory " + dir.getAbsolutePath()
                         + ". If directory is given on command line, only sub dirs with server names are expected.");
            }

			for (File logFile : logFiles) {
				try {
					processLogFile(logFile, feeder);
				} catch (IOException e) {
					log.error("Error processing file:" + logFile + ". Will continue.", e);
				}
			}

		}

		long endTime = System.currentTimeMillis();
		long durationInMillis = endTime - startTime;
		log.info("Processing time: " + durationInMillis / 1000 + " seconds.");

	}

    @Override
    public String toString() {
		return "FileFeeder{" + "filterPatternIncludes=" + filterPatternIncludes +
			", filterPatternExcludes=" + filterPatternExcludes +
			", skipLines=" + skipLines +
			'}';
    }
}


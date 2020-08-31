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
package nl.stokpop.lograter.util;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.util.time.DateUtils;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

	private static final Pattern PATTERN_NON_FILE_CHARS = Pattern.compile("[^a-zA-Z0-9.-]");
    public static final int FOUR_MB = 4 * 1024 * 1024;

    private FileUtils() {}

    /**
     * Returns relative path for completePath from rootPath
     */
    public static String findRelativePath(File rootPath, File completePath) {
        return rootPath.toURI().relativize(completePath.toURI()).getPath();
    }

    public static BufferedReader createBufferedReader(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);

        final String lowerCaseFilename = file.getName().toLowerCase();
        if (lowerCaseFilename.endsWith(".gz")) {
            inputStream = new GZIPInputStream(inputStream);
        }
        else if (lowerCaseFilename.endsWith(".zip")) {
            inputStream = new ZipInputStream(inputStream);
            ((ZipInputStream)inputStream).getNextEntry();
        }
        else if (lowerCaseFilename.endsWith(".bz2")) {
            inputStream = new BZip2CompressorInputStream(inputStream);
        }

        return FileUtils.createBufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    public static List<File> findFilesThatMatchFilenames(List<String> files) {
	    if (files == null) {
		    log.warn("Empty file list given! Return empty list of files.");
		    return Collections.emptyList();
	    }

        List<File> filesToProcess = new ArrayList<>();

	    // filter files that are contained in other file names, to avoid double files below
	    List<String> filteredFiles = new ArrayList<>(files);
	    for (String filename : files) {
		    for (String otherFile : files) {
			    if (otherFile.contains(filename) && !otherFile.equals(filename)) {
				    filteredFiles.remove(otherFile);
			    }
		    }
	    }

        for (String filename : filteredFiles) {
            File file = new File(filename);

            if (file.isDirectory()) {
                // createReport all files in the directory
                log.error("Found directory in file list. Directories are not supported. Skipping: {}", file);
                continue;
            }

	        File containerDir = file.getAbsoluteFile().getParentFile();

            if (containerDir == null || !containerDir.exists()) {
                throw new LogRaterException("Container directory of file [" + filename + "] does not exist: " + containerDir);
            }

            final String filenamePrefix = file.getName();
            File[] filesToFeed = containerDir.listFiles((directory, name) -> name.startsWith(filenamePrefix));

            if ((filesToFeed != null ? filesToFeed.length : 0) == 0) {
                throw new LogRaterException("No files found in [" + containerDir + "] that start with: [" + filenamePrefix + "]");
            }
            else {
                filesToProcess.addAll(Arrays.asList(filesToFeed));
            }
        }
        return filesToProcess;
    }

    public static BufferedReader createBufferedReader(Reader reader) {
        BufferedReader bufferedReader;
        if (reader instanceof BufferedReader) {
            bufferedReader = (BufferedReader) reader;
        }
        else {
            bufferedReader = new BufferedReader(reader, FOUR_MB);
        }
        return bufferedReader;
    }

    public static InputStreamReader createBufferedInputStreamReader(InputStream inputStream) {
        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

    public static String createFilenameWithTimestampFromPathOrUrl(String name, String extention) {
        String filename = replaceNonFileChars(name) + "." + System.currentTimeMillis() + extention;
        if (filename.length() > 30) {
            filename = name.hashCode() + System.currentTimeMillis() + extention;
        }
        return filename;
    }

	public static void deleteDir(File tempDir) {
		File[] files = tempDir.listFiles();
		if (files != null) {
			for(File file : files) {
				if(file.isDirectory()) {
					deleteDir(file);
				} else {
					boolean deleteOK = file.delete();
					if (!deleteOK) throw new RuntimeException("Cannot delete file " + file.getAbsolutePath());
				}
			}
		}
		boolean deleteOK = tempDir.delete();
		if (!deleteOK) throw  new RuntimeException("Cannot delete temp dir " + tempDir.getAbsolutePath());
	}

	public static String replaceNonFileChars(String filename) {
		return PATTERN_NON_FILE_CHARS.matcher(filename).replaceAll("_");
	}

    /**
     * Return first line of a file.
     * @param filePath a path to a file
     * @return first line or empty string if there is no first line
     * @throws IOException when file cannot be read
     */
    public static String readFirstLine(final String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.lines(path).findFirst().orElse("");
    }

    /**
     * Based on report dir and filepath, create path with reportDir prepended if
     * given filepath is not absolute. Also replaces {ts} in the path.
     */
    public static File createFullOutputReportPath(String reportDirectory, String filepath) {
        final String pathWithTimestamp = DateUtils.replaceTimestampMarkerInFilename(filepath);
        File outputPath = new File(pathWithTimestamp);

        if (!outputPath.isAbsolute()) {
            outputPath = new File(reportDirectory, outputPath.getPath());
        }
        return outputPath;
    }

    public static PrintWriter createBufferedPrintWriterWithUTF8(File file) throws FileNotFoundException {
        FileOutputStream outputStream = new FileOutputStream(file);
        return createBufferedPrintWriterWithUTF8(outputStream);
    }

    public static PrintWriter createBufferedPrintWriterWithUTF8(OutputStream outputStream) {
        return new PrintWriter(new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(outputStream), StandardCharsets.UTF_8)));
    }

}

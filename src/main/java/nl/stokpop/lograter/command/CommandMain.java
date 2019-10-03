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
package nl.stokpop.lograter.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.graphs.AbstractGraphCreator;
import nl.stokpop.lograter.util.time.TimeMeasurementStorageTypeConverter;

import java.util.UUID;

@Parameters(separators = "=", commandDescription = "Main commands.")
public class CommandMain {

    private static final String DEFAULT_REPORTS_DIR = "reports";

    private String logFileRaterVersion = "Unknown";

	@Parameter(names = {"-h", "--help"},
            description = "Display usage.", help = true, hidden = true)
	public boolean help = false; 

	@Parameter(names = {"-debug"},
            description = "Print stacktraces with errors.")
	public boolean debug = false;

	@Parameter(names = {"-storage"},
            description = "The type of storage to use. Options: mem (in memory, default), externalsort or database (in sqlite database)", converter=TimeMeasurementStorageTypeConverter.class)
	public CounterStorageType storage = CounterStorageType.Memory;

	@Parameter(names = {"-storage.dir"},
            description = "Where to store files for externalsort or database (in sqlite database). Defaults to working directory.")
	public String storageDir = ".";

	@Parameter(names = {"-o", "--output.file"},
            description = "Write to this file. If not set writes to std out. Use {ts} to include a timestamp in your filename.")
	public String outputFilename;
	
	@Parameter(names = {"-r", "--report.dir"},
            description = "The directory for the reports (an html file with graphs in this release). Use {ts} to include a timestamp in your directory. Defaults to " + DEFAULT_REPORTS_DIR + " in current dir.")
	public String reportDirectory = DEFAULT_REPORTS_DIR;
	
	@Parameter(names = {"-st", "--starttime" },
            description = "The start time of filter: " + AbstractGraphCreator.STANDARD_TIME_FORMAT_STRING)
	public String startTimeStr;
	
	@Parameter(names = {"-et", "--endtime" },
            description = "The end time of filter: " + AbstractGraphCreator.STANDARD_TIME_FORMAT_STRING)
	public String endTimeStr;

	@Parameter(names = {"-clear-db" },
            description = "Clear the database.")
	public boolean clearDb = false;
	
	@Parameter(names = {"-use-db" },
            description = "Only use database input, skip file parsing.")
	public boolean useDb = false;

	@Parameter(names = {"-runid" },
            description = "A run id to identify a test run with a report. Is displayed in reports.")
	public String runId = UUID.randomUUID().toString();

	@Parameter(names = {"-base-unit" },
            description = "What base unit to use in reports. Choose from: milliseconds, microseconds. Default is milliseconds.")
	public BaseUnit baseUnit = BaseUnit.milliseconds;

    @Override
    public String toString() {
        return "CommandMain{" +
                "logFileRaterVersion='" + logFileRaterVersion + '\'' +
                ", help=" + help +
                ", debug=" + debug +
                ", baseUnit=" + baseUnit +
                ", storage=" + storage +
                ", baseUnit=" + baseUnit +
                ", outputFilename='" + outputFilename + '\'' +
                ", reportDirectory='" + reportDirectory + '\'' +
                ", startTimeStr='" + startTimeStr + '\'' +
                ", endTimeStr='" + endTimeStr + '\'' +
                ", clearDb=" + clearDb +
                ", useDb=" + useDb +
                ", runId='" + runId + '\'' +
                '}';
    }

    public String getLogFileRaterVersion() {
        return logFileRaterVersion;
    }

    public void setLogFileRaterVersion(String logFileRaterVersion) {
        this.logFileRaterVersion = logFileRaterVersion;
    }

}

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
package nl.stokpop.lograter.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import nl.stokpop.lograter.graphs.AbstractGraphCreator;

import java.util.List;

@Parameters(separators = "=", commandDescription = "Parse WebSphere verbose garbage collection log files.")
public class CommandGcVerboseLog extends LogRaterCommand {

    private static final String COMMANDNAME = "gc";
    private static final String DEFAULT_VERBOSE_GC_REPORT_TXT_FILE = "verbose-gc-report-{ts}.txt";

    @Parameter(description = "List of gc verbose files to parse. Websphere Application Server 7 or 8 style supported. OpenJ9 gc verbose might work too.")
    public List<String> files;

    @Parameter(names = { "--report-file" }, description = "Text file to write output to. Defaults to " + DEFAULT_VERBOSE_GC_REPORT_TXT_FILE + " in current dir.")
    public String reportFile = DEFAULT_VERBOSE_GC_REPORT_TXT_FILE;

    @Parameter(names = {"-st-analysis", "--starttime-analysis" }, description = "The start time of analysis period: " + AbstractGraphCreator.STANDARD_TIME_FORMAT_STRING)
    public String startTimeAnalysisStr;

    @Parameter(names = {"-et-analysis", "--endtime-analysis" }, description = "The end time of analysis period: " + AbstractGraphCreator.STANDARD_TIME_FORMAT_STRING)
    public String endTimeAnalysisStr;

    @Parameter(names = {"-st-memory-fit", "--starttime-memory-fit" }, description = "The start time of memory fit period: " + AbstractGraphCreator.STANDARD_TIME_FORMAT_STRING)
    public String startTimeMemoryFitStr;

    @Parameter(names = {"-et-memory-fit", "--endtime-memory-fit" }, description = "The end time of memory fit period: " + AbstractGraphCreator.STANDARD_TIME_FORMAT_STRING)
    public String endTimeMemoryFitStr;

    @Override
    public String toString() {
        return "CommandGcVerboseLog{" +
                "files=" + files +
                ", reportFile='" + reportFile + '\'' +
                ", startTimeAnalysisStr='" + startTimeAnalysisStr + '\'' +
                ", endTimeAnalysisStr='" + endTimeAnalysisStr + '\'' +
                ", startTimeMemoryFitStr='" + startTimeMemoryFitStr + '\'' +
                ", endTimeMemoryFitStr='" + endTimeMemoryFitStr + '\'' +
                '}';
    }

	@Override
	public String getCommandName() {
		return COMMANDNAME;
	}

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}

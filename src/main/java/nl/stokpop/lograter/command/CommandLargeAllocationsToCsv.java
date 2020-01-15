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

import java.util.List;

@Parameters(separators = "=", commandDescription = "Parse WebSphere application server logs with large allocation traces.")
public class CommandLargeAllocationsToCsv extends LogRaterCommand {
    private static final String COMMANDNAME = "alloc";

    private static final String DEFAULT_LARGE_ALLOCATIONS_LOG_CSV_FILE = "large-allocations-log-{ts}.csv";

    @Parameter(description = "List of files to parse. Normally the native_stderr.log of Websphere Application Server.")
    public List<String> files;

    @Parameter(names = { "--csv-file" },
            description = "Csv file to write output to. Defaults to " + DEFAULT_LARGE_ALLOCATIONS_LOG_CSV_FILE + " in current dir.")
    public String csvFile = DEFAULT_LARGE_ALLOCATIONS_LOG_CSV_FILE;

    @Override
    public String toString() {
        return "CommandLargeAllocationsToCsv{" +
                "files=" + files +
                ", csvFile='" + csvFile + '\'' +
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

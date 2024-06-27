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

@Parameters(separators = "=", commandDescription = "Transform an access log to a csv file.")
public class CommandAccessLogToCsv extends AbstractCommandMapperAndClickPath {

    private static final String COMMANDNAME = "accessToCsv";
    private static final String DEFAULT_ACCESS_LOG_CSV_FILE = "access-log-{ts}.csv";

    @Parameter(names = { "--csv-file" },
            description = "Csv file to write output to. Defaults to " + DEFAULT_ACCESS_LOG_CSV_FILE + " in current dir.")
    public String csvFile = DEFAULT_ACCESS_LOG_CSV_FILE;

    @Override
    public String toString() {
        return "CommandAccessLogToCsv{" +
                "csvFile='" + csvFile + '\'' +
                "} " + super.toString();
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

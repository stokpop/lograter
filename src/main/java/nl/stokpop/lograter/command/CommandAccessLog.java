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
package nl.stokpop.lograter.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Parse an access log file.")
public class CommandAccessLog extends AbstractCommandAccessLog {
	
	private static final String COMMANDNAME = "access";

	public enum LogType { apache, nginx }

    @Parameter(names = { "-log-type" }, description = "Type of log file: apache (default) or nginx. " +
            "Use apache logformat (e.g. %t, %D, etc...) or use nginx style log format (e.g. with $request, $status, ...")
    public LogType logType = LogType.apache;

    @Override
    public String toString() {
        return "CommandAccessLog{" +
            "logType=" + logType +
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

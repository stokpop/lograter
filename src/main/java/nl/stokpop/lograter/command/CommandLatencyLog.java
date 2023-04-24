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
package nl.stokpop.lograter.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Parse a log file that contains latency numbers.")
public class CommandLatencyLog extends AbstractCommandMapperAndClickPath {

	private static final String COMMAND_NAME = "latency";

    public CommandLatencyLog() {
        logPattern = "%d;%X{sessionId};%X{service};%X{operation};%X{latency}%n";
    }

	@Parameter(names = { "-cf", "--counter-fields" }, description = "Counter fields to use for counting. Comma separated list of field names.")
	public String counterFields = "service,operation";

	@Parameter(names = { "-latency-field" }, description = "Field used for latency. Also specify the latency unit!")
	public String latencyField = "latency";

    @Parameter(names = { "-latency-unit" }, description = "Unit used for latency: seconds, milliseconds, microseconds, nanoseconds. Default is milliseconds.")
    public LatencyUnit latencyUnit = LatencyUnit.milliseconds;

    @Parameter(names = { "-failure-field" }, description = "Field used for failure detection. Also specify the failure type. Default is empty.")
	public String failureField = null;

    @Parameter(names = { "-failure-field-type" }, description = "Type of the failure field: bool, http, regexp. Default is http.")
    public FailureFieldType failureFieldType = FailureFieldType.http;

    @Parameter(names = { "-failure-field-regexp" }, description = "The regular expression to determine failures. If it matches, it is a failure, otherwise success.")
    public String failureFieldRegexp = "error";

    @Override
    public String toString() {
        return "CommandLatencyLog{" +
            "counterFields='" + counterFields + '\'' +
            ", latencyField='" + latencyField + '\'' +
            ", latencyUnit=" + latencyUnit +
            ", failureField='" + failureField + '\'' +
            ", failureFieldType=" + failureFieldType +
            ", failureFieldRegexp='" + failureFieldRegexp + '\'' +
            "} " + super.toString();
    }

    @Override
	public String getCommandName() {
		return COMMAND_NAME;
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

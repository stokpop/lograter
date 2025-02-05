/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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
import nl.stokpop.lograter.processor.jmeter.JMeterLogLineType;

@Parameters(separators = "=", commandDescription = "Parse jtl file from a jMeter run.")
public class CommandJMeter extends AbstractCommandAccessLog {
	
	private static final String COMMANDNAME = "jmeter";

	@Parameter(names = {"--report-logline-type" }, description = "The logline type to use in the report. Choose: sample, transaction, all. Default: sample (e.g. http requests)")
	public JMeterLogLineType jMeterLogLineTypeToReport = JMeterLogLineType.SAMPLE;


	@Override
	public String toString() {
		return "CommandJMeter [toString()=" + super.toString() + "]";
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

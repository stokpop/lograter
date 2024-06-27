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
package nl.stokpop.lograter.processor.applicationlog;

import nl.stokpop.lograter.logentry.LogbackLogEntry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationLogProcessorTest {

	@Test
	public void processEntry() {
		ApplicationLogConfig config = new ApplicationLogConfig();
		ApplicationLogProcessor processor = new ApplicationLogProcessor(config);
		// should not throw exceptions
		processor.processEntry(new LogbackLogEntry());

		ApplicationLogData data = processor.getData();
		assertEquals(1, data.getTotalLogLines());
	}

}
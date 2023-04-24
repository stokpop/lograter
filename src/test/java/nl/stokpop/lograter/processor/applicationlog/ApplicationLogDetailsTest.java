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
package nl.stokpop.lograter.processor.applicationlog;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

public class ApplicationLogDetailsTest {

	@Test
	public void getNonLogLinesImmutable() {
		String message = "message";
		String[] nonLogLines = new String[] {"line1", "line2"};
		ApplicationLogDetails details = new ApplicationLogDetails(message, nonLogLines);
		String ghostline = "ghostline";
		nonLogLines[0] = ghostline;
		String messageDetails = details.getDetails();
		assertFalse(messageDetails.contains(ghostline));
	}

	@Test
	public void getDetailsNewLine() {
		String message = "message";
		String[] nonLogLines = new String[] {"line1", "line2"};
		ApplicationLogDetails details = new ApplicationLogDetails(message, nonLogLines);
		String messageDetails = details.getDetails();
		assertEquals("line1\nline2", messageDetails);
	}

	@Test
	public void nullValues() {
		ApplicationLogDetails details = new ApplicationLogDetails(null, (String[]) null);
		assertNull(details.getMessage());
		assertNull(details.getDetails());
	}

	@Test
	public void nullValuesCompare() {
		ApplicationLogDetails detailsNull1 = new ApplicationLogDetails(null, (String[]) null);
		ApplicationLogDetails detailsNull2 = new ApplicationLogDetails(null, (String[]) null);
		ApplicationLogDetails detailsM2 = new ApplicationLogDetails("m", new String[] {""});
		ApplicationLogDetails detailsMNull3 = new ApplicationLogDetails("m", (String[]) null);

		assertEquals(detailsNull1, detailsNull2);
		assertEquals(detailsNull1.hashCode(), detailsNull2.hashCode());

		assertEquals(-1, detailsNull1.compareTo(detailsM2));
		assertEquals(1, detailsM2.compareTo(detailsNull1));
		assertEquals(0, detailsNull1.compareTo(detailsNull2));
		assertEquals(1, detailsM2.compareTo(detailsMNull3));

		// Nulls first?
		List<ApplicationLogDetails> list = new ArrayList<>();
		list.add(detailsM2);
		list.add(detailsMNull3);
		Collections.sort(list);
		assertEquals(detailsMNull3, list.get(0));
	}

	@Test
	public void nullMessageCompare() {
		ApplicationLogDetails detailsNull1 = new ApplicationLogDetails(null, new String[] {"one\n" , "two\n"});
		ApplicationLogDetails detailsNull2 = new ApplicationLogDetails(null, new String[] {"one\n" , "two\n"});

		assertEquals(detailsNull1, detailsNull2);
		assertEquals(detailsNull1.hashCode(), detailsNull2.hashCode());
	}

	@Test
	public void nullMessageNotCompare() {
		ApplicationLogDetails detailsNull1 = new ApplicationLogDetails(null, new String[] {"two\n" , "one\n"});
		ApplicationLogDetails detailsNull2 = new ApplicationLogDetails(null, new String[] {"one\n" , "two\n"});

		assertNotSame(detailsNull1, detailsNull2);
		assertNotSame(detailsNull1.hashCode(), detailsNull2.hashCode());
	}

}
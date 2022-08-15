/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConcurrentSoftCacheTest {

	@Test
	public void testGet() {
		ConcurrentSoftCache<String, String> map = new ConcurrentSoftCache<>();

		String key = "my-key";
		String value = "my-value";

		map.put(key, value);

		String retrievedValue = map.get(key);

		// in theory this could fail in a low memory situation: null would be returned in that case
		assertEquals(value, retrievedValue);

	}
}
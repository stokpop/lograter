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
package nl.stokpop.lograter.util.time;

import nl.stokpop.lograter.LogRaterException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SessionDurationCalculatorTest {

	@Test(expected = LogRaterException.class)
	public void testAddHitWithNullId() {
		SessionDurationCalculator calculator = new SessionDurationCalculator(60);
		calculator.addHit(null, 1000);
	}

	@Test
	public void testGetAvgSessionDuration() {
		SessionDurationCalculator calculator = new SessionDurationCalculator(60);
		calculator.addHit("session-1", 1000);
		calculator.addHit("session-1", 2000);
		calculator.addHit("session-1", 3000);
		calculator.addHit("session-1", 4000);
		calculator.addHit("session-1", 5000);
		calculator.addHit("session-1", 6000);
		calculator.addHit("session-1", 7000);
		calculator.addHit("session-2", 10000);
		calculator.addHit("session-2", 11000);
		calculator.addHit("session-2", 12000);
		calculator.addHit("session-2", 13000);
		calculator.addHit("session-2", 14000);
		calculator.addHit("session-2", 15000);
		calculator.addHit("session-2", 16000);

		long avgSessionDuration = calculator.getAvgSessionDuration();

		assertEquals(6000, avgSessionDuration);



	}
}
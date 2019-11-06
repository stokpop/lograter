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
package nl.stokpop.lograter.util;

public class LogRaterUtils {

	private LogRaterUtils() {}

	/**
	 * Utility method to filter on printing a lot of the same errors.
	 * @param currentCount supply a count of number of errors, for example
	 * @return true if count is a modulo of a factor of 10 for the current count
	 */
	public static boolean isMod10Count(long currentCount) {
		int moduloToUse =
				currentCount > 1000000 ? 1000000 :
					currentCount > 100000 ? 100000 :
						currentCount > 10000 ? 10000 :
							currentCount > 1000 ? 1000 :
								currentCount > 100 ? 100 :
									currentCount > 10 ? 10 : 1;
		return currentCount % moduloToUse == 0;
	}

	public static boolean isEmpty(String value) {
		return value == null || value.trim().length() == 0;
	}

}

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
package nl.stokpop.lograter.sar;

import nl.stokpop.lograter.util.time.TimePeriod;

/**
 * Holds the parsing state.
 */
public class ParseContext {
    private String currentLine;
    private long fileDate;
    private TimePeriod timePeriod;

    public String getCurrentLine() {
        return currentLine;
    }
    public void setCurrentLine(String line) {
        this.currentLine = line;
    }

    public void setFileDate(long fileDate) {
        this.fileDate = fileDate;
    }

    public long getFileDate() {
        return fileDate;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public boolean isWithinTimePeriod(long timestamp) {
	    return timePeriod == null || timePeriod.isWithinTimePeriod(timestamp);
    }

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
	
}

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
package nl.stokpop.lograter.sar.entry;

/**
 * Holds Swap info from SAR file.
 */
public class SarSwapEntry extends SarEntry {

    private final double pagesSwappedInPerSec;
    private final double pagesSwappedOutPerSec;

    public SarSwapEntry(long timestamp, double pagesSwappedInPerSec, double pagesSwappedOutPerSec) {
        super(timestamp);
        this.pagesSwappedInPerSec = pagesSwappedInPerSec;
        this.pagesSwappedOutPerSec = pagesSwappedOutPerSec;
    }

    public double getPagesSwappedInPerSec() {
        return pagesSwappedInPerSec;
    }

    public double getPagesSwappedOutPerSec() {
        return pagesSwappedOutPerSec;
    }

    @Override
    public String toString() {
        return "SarSwapEntry{" +
                super.toString() +
                " pagesSwappedInPerSec=" + pagesSwappedInPerSec +
                ", pagesSwappedOutPerSec=" + pagesSwappedOutPerSec +
                "} " ;
    }
}

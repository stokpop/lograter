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
package nl.stokpop.lograter.clickpath;

import nl.stokpop.lograter.counter.SimpleCounter;

import java.util.Map;

public interface ClickPathCollector {
    
    void addClickPath(ClickPath clickPath);

    Map<String, SimpleCounter> getClickPaths();

    String getExampleSessionIdForClickPath(String path);

    long getAvgSessionDurationForClickPath(String path);

    String getPathAsStringWithAvgDuration(String path);

    long getClickPathLength(String path);
}

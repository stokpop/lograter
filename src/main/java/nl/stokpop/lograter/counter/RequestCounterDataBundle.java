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
package nl.stokpop.lograter.counter;

import nl.stokpop.lograter.processor.BasicCounterLogConfig;
import nl.stokpop.lograter.store.RequestCounterStorePair;

import java.util.List;

/**
 * DataBundles for RequestCounterStores.
 */
public interface RequestCounterDataBundle {

	RequestCounterStorePair getTotalRequestCounterStorePair();

	List<RequestCounterStorePair> getRequestCounterStorePairs();

	boolean doesSupportFailureRequestCounters();

	BasicCounterLogConfig getConfig();
}

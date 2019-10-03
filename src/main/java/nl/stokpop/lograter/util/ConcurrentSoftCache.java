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

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent map with soft references to be used as cache. The garbage collect will claim the references memory
 * if needed, when an OutOfMemoryException is on the horizon.
 */
public class ConcurrentSoftCache<K, V> {

	private ConcurrentHashMap<K, SoftReference<V>> map = new ConcurrentHashMap<>();

	public V get(Object key) {
		SoftReference<V> value = map.get(key);
		if (value != null && value.get() != null) {
			return value.get();
		} else {
			map.remove(key);
			return null;
		}
	}

	public V put(K key, V value) {
		SoftReference<V> oldValue = map.put(key, new SoftReference<>(value));
		return oldValue == null ? null : oldValue.get();
	}
}


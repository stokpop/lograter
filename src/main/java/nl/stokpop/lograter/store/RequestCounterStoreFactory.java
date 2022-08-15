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
package nl.stokpop.lograter.store;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.util.DatabaseBootstrap;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;

public class RequestCounterStoreFactory {

	private static final Logger log = LoggerFactory.getLogger(RequestCounterStoreFactory.class);

    private final CounterStorageType type;
	private Connection con;
	private final File storageRootDir;

	private final TimePeriod timePeriod;

	public RequestCounterStoreFactory(CounterStorageType type, TimePeriod timePeriod, File storageRootDir) {
        log.info(
                "Create RequestCounterStoreFactory with storage type [{}] for time period [{}] and with storage dir [{}]."
                , type, timePeriod, storageRootDir);

        this.type = type;
        this.timePeriod = timePeriod;
        if (type == CounterStorageType.Database) {
            con = DatabaseBootstrap.instance().getDatabaseConnection();
        }
        if (storageRootDir == null) {
            storageRootDir = new File(".");
        }
        this.storageRootDir = storageRootDir;
    }

	public RequestCounterStoreFactory(CounterStorageType type, TimePeriod timePeriod) {
	    this(type, timePeriod, null);
	}

	public RequestCounterStoreFactory(CounterStorageType type) {
		this(type, TimePeriod.MAX_TIME_PERIOD);
	}

	public RequestCounterStoreFactory(CounterStorageType type, File storageRootDir) {
        this(type, TimePeriod.MAX_TIME_PERIOD, storageRootDir);
    }

    public RequestCounterStore newInstance(String storeName, CounterKey totalRequestsKey, int maxUniqueCounters) {
	    RequestCounterStore store;
	    switch (type) {
			case Memory:
				store = new RequestCounterStoreHashMap(storeName, totalRequestsKey, timePeriod);
				break;
			case Database:
				store = new RequestCounterStoreSqLite(storeName, totalRequestsKey.getName(), con, timePeriod);
				break;
			case ExternalSort:
			    if (storageRootDir == null) {
			        throw new LogRaterException("Unable to create an external sort request counter without supplying a storage dir.");
                }
				store = new RequestCounterStoreExternalSort(storageRootDir, storeName, totalRequestsKey, timePeriod);
			    break;
			default:
				log.warn("No valid measurement store option found: {}, using in memory store.", type);
				store = new RequestCounterStoreHashMap(storeName, totalRequestsKey, timePeriod);
		}
		// always limit the max number of requests to avoid memory issues and slow behaviour
		return new RequestCounterStoreMaxCounters(store, maxUniqueCounters);
	}

	public RequestCounterStore newInstance(String storeName, int maxUniqueCounters) {
		return newInstance(storeName, CounterKey.of(storeName + "-total"), maxUniqueCounters);
	}

}

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
package nl.stokpop.lograter.store;

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

@NotThreadSafe
public class RequestCounterStoreSqLite implements RequestCounterStore {

	private static final Logger log = LoggerFactory.getLogger(RequestCounterStoreSqLite.class);
	
    private final String name;
	private final Connection con;
	private final long dbCounterStoreId;
	private final Map<CounterKey, Long> counterKeyToDbCounterIdMapper;
	private final Map<CounterKey, RequestCounter> cachedCounters;
	private final RequestCounter totalRequestCounter;
	private final TimePeriod timePeriod;

	public RequestCounterStoreSqLite(String storeName, String totalCounterName, Connection con, TimePeriod timePeriod) {
		this.name = storeName;
		this.timePeriod = timePeriod;
		if (con == null) {
			throw new LogRaterException("Connection cannot be null!");
		}
		this.con = con;
		try {
			this.dbCounterStoreId = fetchCounterStoreIdOrCreateNewOne();
			this.counterKeyToDbCounterIdMapper = fillCounterKeyToDbCounterIdMapper(con, dbCounterStoreId);
			this.cachedCounters = fetchCountersForCounterStoreFromDb(con, dbCounterStoreId, timePeriod);
			this.totalRequestCounter = fetchTotalRequestCounterOrCreateNewOne(con, totalCounterName, dbCounterStoreId, timePeriod);
		} catch (SQLException e) {
			throw new LogRaterException("Problem getting data for " + storeName, e);
		}
	}

	private RequestCounter fetchTotalRequestCounterOrCreateNewOne(Connection con, String totalCounterName, long counterStoreId, TimePeriod timePeriod) throws SQLException {

		long totalCounterId;

		try {
            try (PreparedStatement queryCount = con.prepareStatement("select id from counter where counter_store_id = ? and is_total_counter = ?")) {
                queryCount.setLong(1, counterStoreId);
                queryCount.setBoolean(2, true);
                try (ResultSet resultset = queryCount.executeQuery()) {
                    if (resultset.next()) {
                        long foundId = resultset.getLong(1);
                        log.debug("Found id {} for total counter: {} for counter store: {}", foundId, totalCounterName, name);
                        totalCounterId = foundId;
                    } else {
                        log.debug("Total counter not found: {} for counter store: {}. Creating new name in database.", totalCounterName, name);
                        totalCounterId = insertTotalCounter(con, counterStoreId, totalCounterName);
                    }
                }
            }
		} finally {
			con.commit();
		}

		TimeMeasurementStore tmStore =  new TimeMeasurementStoreSqLite(totalCounterName, totalCounterId, con, timePeriod);
		return new RequestCounter(CounterKey.of(totalCounterName), tmStore);

	}

	private static Map<CounterKey, RequestCounter> fetchCountersForCounterStoreFromDb(Connection con, long dbCounterStoreId, TimePeriod timePeriod) {
		Map<CounterKey, RequestCounter> counters = new HashMap<>();
		try {
			try {
                try (PreparedStatement query = con.prepareStatement("select name, id from counter where counter_store_id = ? and is_total_counter = ?")) {
                    query.setLong(1, dbCounterStoreId);
                    query.setBoolean(2, false);
                    try (ResultSet resultSet = query.executeQuery()) {
                        while (resultSet.next()) {
                            String name = resultSet.getString(1);
                            long counterId = resultSet.getLong(2);
                            log.debug("Found id {} for counter: {}", counterId, name);
                            TimeMeasurementStore tmStore = new TimeMeasurementStoreSqLite(name, counterId, con, timePeriod);
                            counters.put(CounterKey.of(name), new RequestCounter(CounterKey.of(name), tmStore));
                        }
                    }
                }
			} finally {
				con.commit();
			}
		} catch (SQLException e) {
			throw new LogRaterException("Cannot fetch counter from db for dbCounterStoreId: " + dbCounterStoreId, e);
		}
		return counters;
	}

	private static Map<CounterKey, Long> fillCounterKeyToDbCounterIdMapper(Connection con, long counterStoreId) throws SQLException {
		Map<CounterKey, Long> mapper = new HashMap<>();
		try {
            try (PreparedStatement query = con.prepareStatement("select name, id from counter where counter_store_id = ?")) {
                query.setLong(1, counterStoreId);
                try (ResultSet resultSet = query.executeQuery()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString(1);
                        long counterId = resultSet.getLong(2);
                        log.debug("Found id {} for counter: {}", counterId, name);
                        mapper.put(CounterKey.of(name), counterId);
                    }
                }
            }
		} finally {
			con.commit();
		}		
		return mapper;
	}

	private long fetchCounterStoreIdOrCreateNewOne() throws SQLException {
		int id;

		try {
            try (PreparedStatement queryCount = con.prepareStatement("select id from counter_store where name = ?")) {
                queryCount.setString(1, name);
                try (ResultSet resultset = queryCount.executeQuery()) {
                    if (resultset.next()) {
                        int foundId = resultset.getInt(1);
                        log.debug("Found id {} for counter_store: {}", foundId, name);
                        id = foundId;
                    } else {
                        log.debug("Counter store not found: {}. Creating new name in database.", name);
                        id = createNewNameInDb();
                    }
                }
            }
		} finally {
			con.commit();
		}
		return id;
	}

	private int createNewNameInDb() throws SQLException {
		log.info("Creating counter_store name {} in db.", name);
		
		int id;

		try {
			String sql = "insert into counter_store(name) values (?)";
            try (PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, name);
                int result = statement.executeUpdate();

                if (result != 1) {
                    throw new LogRaterException("Insert failed:" + sql + " for name: " + name);
                }
                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    resultSet.next();
                    id = resultSet.getInt(1);
                }
            }
		} finally {
			con.commit();
		}
		return id;
	}

	public void add(CounterKey counterKey, long logTimestamp, int durationMillis) {
		RequestCounter counter = addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(counterKey);
		counter.incRequests(logTimestamp, durationMillis);
		totalRequestCounter.incRequests(logTimestamp, durationMillis);
	}

	private long insertCounter(Connection con, long dbCounterStoreId, String counterName) throws SQLException {
		return insertCounter(con, dbCounterStoreId, counterName, false);
	}

	private long insertTotalCounter(Connection con, long dbCounterStoreId, String counterName) throws SQLException {
		return insertCounter(con, dbCounterStoreId, counterName, true);
	}

	private long insertCounter(Connection con, long dbCounterStoreId, String counterName, boolean isTotalCounter) throws SQLException {

		log.info("Creating counter name {} in db for counter store {}", counterName, name);
		
		long id;

		try {
			String sql = "insert into counter(name, counter_store_id, is_total_counter) values (?, ?, ?)";
            try (PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, counterName);
                statement.setLong(2, dbCounterStoreId);
                statement.setBoolean(3, isTotalCounter);

                int result = statement.executeUpdate();

                if (result != 1) {
                    throw new LogRaterException("Insert failed:" + sql + " for name: " + counterName);
                }

                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    resultSet.next();
                    id = resultSet.getLong(1);
                }
            }

		} finally {
			con.commit();
		}

		return id;
	}

	@Override
	public String toString() {
		return "CounterStoreSqLite [name=" + name + "]";
	}

	public Iterator<RequestCounter> iterator() {
		return cachedCounters.values().iterator();
	}

	@Override
	public boolean isEmpty() {
		return cachedCounters.isEmpty();
	}

    @Override
	public RequestCounter addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(CounterKey key) {
		try {
			if (!counterKeyToDbCounterIdMapper.containsKey(key)) {
				String counterName = key.getName();
				long dbCounterId = insertCounter(con, dbCounterStoreId, counterName);
				counterKeyToDbCounterIdMapper.put(key, dbCounterId);
				RequestCounter counter = new RequestCounter(key, new TimeMeasurementStoreSqLite(counterName, dbCounterId, con, timePeriod));
				cachedCounters.put(key, counter);
				return counter;
			}
			else {
				return cachedCounters.get(key);
			}
		} catch (SQLException e) {
			throw new LogRaterException(String.format("Failed to insert new counter [%s]", key), e);
		}
	}

	@Override
	public RequestCounter get(CounterKey key) {
		return cachedCounters.get(key);
	}

	@Override
	public boolean contains(CounterKey key) {
		return counterKeyToDbCounterIdMapper.containsKey(key);
	}

	@Override
	public String getName() {
		return name;
	}

    @Override
    public Set<CounterKey> getCounterKeys() {
        return Collections.unmodifiableSet(cachedCounters.keySet());
    }

	@Override
	public RequestCounter getTotalRequestCounter() {
		if (totalRequestCounter != null) {
			return totalRequestCounter;
		}
		else {
			throw new LogRaterException("Total request counter should exist after creation of counter store: " + this);
		}
	}

}

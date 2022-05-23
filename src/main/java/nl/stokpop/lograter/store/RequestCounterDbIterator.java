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
package nl.stokpop.lograter.store;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class RequestCounterDbIterator implements Iterator<RequestCounter> {
	
	private Iterator<String> countersKeys;
	private Connection con;
	private long dbCounterStoreId;

	private TimePeriod timePeriod;

	public RequestCounterDbIterator(Set<String> counterKeys, long dbCounterStoreId, Connection con, TimePeriod timePeriod) {
		// defensive copy
		countersKeys = new ArrayList<>(counterKeys).iterator();
		this.dbCounterStoreId = dbCounterStoreId;  
		this.con = con;
		this.timePeriod = timePeriod;
	}
	
	@Override
	public boolean hasNext() {
		return countersKeys.hasNext();
	}

	@Override
	public RequestCounter next() {
		String nextCounterKey = countersKeys.next();
		long counterId = fetchCounterIdFromDb(con, dbCounterStoreId, nextCounterKey);
		return new RequestCounter(CounterKey.of(nextCounterKey), new TimeMeasurementStoreSqLite(nextCounterKey, counterId, con, timePeriod));
	}

	private static long fetchCounterIdFromDb(Connection con, long dbCounterStoreId, String counterKey) {
		PreparedStatement query = null;
		ResultSet resultSet = null;
		long counterId = -1;

		try {
			try {
				query = con.prepareStatement("select id from counter where counterstoreid = ? and name = ?");
				query.setLong(1, dbCounterStoreId);
				query.setString(2, counterKey);
				resultSet = query.executeQuery();
				if (resultSet.next()) {
					counterId = resultSet.getLong(1);
				}		
		
	
			} finally {
				if (resultSet != null) resultSet.close();
				if (query != null) query.close();			
				if (con != null) con.commit();
			}
		} catch (SQLException e) {
			throw new LogRaterException("Cannot get counterstore id from database. " +
                    "CounterstoreId: " + dbCounterStoreId + " counterKey: " + counterKey, e);
		}
		return counterId;
	}

	@Override
	public void remove() {
		throw new LogRaterException("Not implemented");
	}

}

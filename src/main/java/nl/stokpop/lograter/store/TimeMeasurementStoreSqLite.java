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

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 * This class is not thread safe. It contains a shared TimeMeasurements cache.
 * 
 * @author Peter Paul Bakker
 */
public class TimeMeasurementStoreSqLite extends AbstractTimeMeasurementStore {
	
	private static final Logger log = LoggerFactory.getLogger(TimeMeasurementStoreSqLite.class);

	private Connection con;
	
	private String name;
	
	private static final int BUFFERSIZE = 100000;
	private long[] timestampsBuffer = new long[BUFFERSIZE];
	private int[] durationsBuffer = new int[BUFFERSIZE];
	private int bufferIdx = 0;

	private boolean isDirty = true;
	private long size;

	private long dbCounterId;
	private TimePeriod timePeriod;

	public TimeMeasurementStoreSqLite(String name, long dbCounterId, Connection connection, TimePeriod timePeriod) {
		this.name = name;
		this.con = connection;
		this.dbCounterId = dbCounterId;
		this.timePeriod = timePeriod;
		this.size = fetchSizeFromDatabase();
	}

	@Override
	public void add(long timestamp, int durationInMillis) {

		isDirty = true;
		timestampsBuffer[bufferIdx] = timestamp;
		durationsBuffer[bufferIdx] = durationInMillis;

		bufferIdx++;
		size++;
	
		if (bufferIdx == BUFFERSIZE) {
			flushBuffer();
		}

		updateFirstAndLastTimestamps(timestamp);

	}

	public void flushBuffer() {
		if (bufferIdx == 0 || !isDirty) {
			log.debug("Nothing to flush for {}", name);
			isDirty = false;
			return;
		}
		log.debug("Flushing {} {} measurements to db.", bufferIdx, name);

		PreparedStatement statement = null;
		try {
			statement = con.prepareStatement("insert into measurement(counter_id, timestamp, duration) values (?, ?, ?)");
			for (int i = 0; i < bufferIdx; i++) {
				statement.setLong(1, dbCounterId);
				statement.setLong(2, timestampsBuffer[i]);
				statement.setInt(3, durationsBuffer[i]);
				statement.addBatch();
			}
			int[] count = statement.executeBatch();
			
			if (count.length != bufferIdx) {
				log.error("Count of batch: {} execute not equal to buffer size: {}", count.length, bufferIdx);
			}

		} catch (SQLException e) {
			throw new LogRaterException("Cannot flush buffer for " + name, e);
		}
		finally {
			bufferIdx = 0;
			isDirty = false;
			try {
				if (statement != null) statement.close();
				con.commit();
			} catch (SQLException e) {
				log.error("Commit failed", e);
			}
		}
	}

	@Override
	public TimePeriod getTimePeriod() {
		if (isDirty) {
			flushBuffer();
		}
		size = fetchSizeFromDatabase();
		return timePeriod.hasBothTimestampsSet() ? timePeriod : super.getTimePeriod();
	}

	@Override
	public TimeMeasurementStore getTimeSlice(TimePeriod timePeriod) {
		// flush the underlying buffers to db, or the timeslice will not see the cached records
		if (isDirty) {
			flushBuffer();
		}
		size = fetchSizeFromDatabase();
		return new TimeMeasurementStoreSqLite(name + "-TimeSlice", dbCounterId, con, timePeriod);
	}

	@Override
	public long getSize() {
		if (!isDirty) {
			return size;
		}
		flushBuffer();
		size = fetchSizeFromDatabase();
		return size;
	}

	private long fetchSizeFromDatabase() {
		try (PreparedStatement queryCount = con.prepareStatement("select count(*) from measurement where counter_id = ? and timestamp >= ? and timestamp <= ?")) {
			queryCount.setLong(1, dbCounterId);
			queryCount.setLong(2, timePeriod.getStartTime());
			queryCount.setLong(3, timePeriod.getEndTime());
			try (ResultSet resultSetCount = queryCount.executeQuery()) {
				if (resultSetCount.next()) {
					long count = resultSetCount.getLong(1);
					log.info("Number of {} measurements to get: {}", dbCounterId, count);
					size = count;
					return count;
				} else {
					log.warn("No measurements found for: {}, returning empty list", name);
					return 0;
				}
			}
		} catch (SQLException e) {
			throw new LogRaterException("Cannot get count of time measurements from database for counterId: " + this.dbCounterId + " name: " + this.name, e);
		} finally {
			try {
				con.commit();
			} catch (SQLException e) {
				log.warn("Error closing resultsets for time measurements from database for counterId: " + this.dbCounterId + " name: " + this.name, e);
			}
		}
	}

	@Override
	public void add(TimeMeasurement timeMeasurement) {
		this.add(timeMeasurement.getTimestamp(), timeMeasurement.getDurationInMillis());
	}

	@Override
	public TimeMeasurementIterator iterator() {
		if (isDirty) {
			// first clear out remaining buffer
			flushBuffer();
			size = fetchSizeFromDatabase();
		}

		final PreparedStatement querySelect;
		final ResultSet resultSetSelect;

		try {
			querySelect = con.prepareStatement("select timestamp, duration from measurement where counter_id = ? and timestamp >= ? and timestamp <= ? order by timestamp");
			querySelect.setLong(1, dbCounterId);
			querySelect.setLong(2, timePeriod.getStartTime());
			querySelect.setLong(3, timePeriod.getEndTime());

			resultSetSelect = querySelect.executeQuery();
			resultSetSelect.setFetchSize(200);

		} catch (SQLException e) {
			throw new LogRaterException("Cannot get time measurements from database for counterId: " + this.dbCounterId + " name: " + this.name, e);
		}

		return new TimeMeasurementIterator() {

			boolean hasNextCalled = false;
			boolean hasNextPrevious = true;

			@Override
			public void close() throws Exception {
				resultSetSelect.close();
				querySelect.close();
				con.commit();
			}

			@Override
			public boolean hasNext() {
				if (hasNextCalled) {
					return hasNextPrevious;
				}
				else {
					try {
						hasNextCalled = true;
						hasNextPrevious = resultSetSelect.next();
						if (!hasNextPrevious) {
							closeWithCatch();
						}
						return hasNextPrevious;
					} catch (SQLException e) {
						throw new LogRaterException("Result set next() failed.", e);
					}
				}
			}

			private void closeWithCatch() {
				try {
					close();
				} catch (Exception e) {
					log.warn("Troubles closing database backed iterator.", e);
				}
			}

			@Override
			public TimeMeasurement next() {
				try {
					if (!hasNextCalled) {
						hasNextPrevious = resultSetSelect.next();
						if (!hasNextPrevious) {
							closeWithCatch();
						}
					}
					if (!hasNextPrevious) {
						throw new NoSuchElementException("No next time measurement element, use hasNext to check!");
					}
					hasNextCalled = false;
					long timestamp = resultSetSelect.getLong(1);
					int duration = resultSetSelect.getInt(2);
					return new TimeMeasurement(timestamp, duration);
				} catch (SQLException e) {
					throw new LogRaterException("Fetching next time measurement from database failed.", e);
				}
			}

			@Override
			public void remove() {
				throw new LogRaterException("Remove is not implemented.");

			}
		};
	}

	@Override
	public boolean isEmpty() {
		return getSize() == 0;
	}

	@Override
    public String toString() {
        return "TimeMeasurementStoreSqLite{" +
                "bufferIdx=" + bufferIdx +
                ", con=" + con +
                ", dbCounterId=" + dbCounterId +
                ", isDirty=" + isDirty +
                ", name='" + name + '\'' +
                ", timePeriod=" + timePeriod +
                '}';
    }

}
